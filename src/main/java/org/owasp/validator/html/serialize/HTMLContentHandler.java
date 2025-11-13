package org.owasp.validator.html.serialize;

import org.xml.sax.ext.DefaultHandler2;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.owasp.validator.html.InternalPolicy;
import org.owasp.validator.html.TagMatcher;
/**
 * Replaces the deprecated HTMLSerializer with a modern SAX ContentHandler.
 * Handles HTML-specific serialization rules per AntiSamy policy.
 */
public class HTMLContentHandler extends DefaultHandler2 { 
    private static final Logger LOGGER = LoggerFactory.getLogger(HTMLContentHandler.class);
    
    private final Writer writer;
    private final InternalPolicy policy;
    private final boolean encodeAllPossibleEntities;
    private final TagMatcher allowedEmptyTags;
    private final TagMatcher requireClosingTags;

    private final Stack<String> elementStack = new Stack<>();
    private static final Set<String> VOID_ELEMENTS =
    Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
        "area", "base", "br", "col", "embed", "hr", "img", "input",
        "link", "meta", "param", "source", "track", "wbr"
    )));
    
    public HTMLContentHandler(Writer writer, InternalPolicy policy) {
        Objects.requireNonNull(writer, "Writer must not be null.");
        Objects.requireNonNull(policy, "Policy must not be null.");
        this.writer = writer;
        this.policy = policy;
        this.allowedEmptyTags = policy.getAllowedEmptyTags();
        this.requireClosingTags = policy.getRequiresClosingTags();
        this.encodeAllPossibleEntities = policy.isEntityEncodeIntlCharacters();
    }
    
    @Override
    public void startElement(String uri, String localName, String qName, 
                            Attributes attrs) throws SAXException {
        try {
            String tagName = qName.toLowerCase();
            elementStack.push(tagName);
            
            // Write opening tag
            writer.write('<');
            writer.write(tagName);
            
            // Write attributes
            for (int i = 0; i < attrs.getLength(); i++) {
                String attrName = attrs.getQName(i).toLowerCase();
                String attrValue = attrs.getValue(i);
                writer.write(' ');
                writer.write(attrName);
                
                if (isURIAttribute(tagName, attrName)) {
                    writer.write("=\"");
                    writer.write(escapeURI(attrValue));
                    writer.write('"');
                } else if (isBooleanAttribute(tagName, attrName)) {
                    // HTML boolean attributes: just write name
                } else {
                    writer.write("=\"");
                    writer.write(escapeAttribute(attrValue));
                    writer.write('"');
                }
            }
            
            // Check if void element or allowed to be empty per policy
            if (isVoidElement(tagName) && 
                policy.getAllowedEmptyTags().matches(tagName)) {
                writer.write('>');  // HTML: no self-closing />
            } else {
                writer.write('>');
            }
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }
    
    @Override
    public void endElement(String uri, String localName, String qName) 
                          throws SAXException {
        try {
            String tagName = qName.toLowerCase();
            
            // Don't write closing tag for void elements
            if (!isVoidElement(tagName)) {
                writer.write("</");
                writer.write(tagName);
                writer.write('>');
            }
            
            if (!elementStack.isEmpty()) {
                elementStack.pop();
            }
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }
    
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        try {
            // Escape content for HTML
            for (int i = start; i < start + length; i++) {
                char c = ch[i];
                switch (c) {
                    case '<': writer.write("&lt;"); break;
                    case '>': writer.write("&gt;"); break;
                    case '&': writer.write("&amp;"); break;
                    case '"': writer.write("&quot;"); break;
                    case '\'': writer.write("&#39;"); break;
                    default:
                        // Handle international characters per policy
                        if (policy.isEntityEncodeIntlCharacters() || 
                            shouldEncodeChar(c)) {
                            writer.write("&#" + (int)c + ";");
                        } else {
                            writer.write(c);
                        }
                }
            }
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }
    
    @Override
    public void comment(char[] ch, int start, int length) throws SAXException {
        // Handle based on policy's comment preservation setting
        if (policy.isPreserveComments()) {
            try {
                writer.write("<!--");
                writer.write(new String(ch, start, length));
                writer.write("-->");
            } catch (IOException e) {
                throw new SAXException(e);
            }
        }
    }
    
    private boolean isVoidElement(String tagName) {
        return VOID_ELEMENTS.contains(tagName);
    }
    
    private boolean isURIAttribute(String tagName, String attrName) {
        // Use HTMLdtd logic or your own mapping
        return "href".equals(attrName) || "src".equals(attrName);
    }
    
    private boolean isBooleanAttribute(String tagName, String attrName) {
        // Boolean HTML attributes
        return "disabled".equals(attrName) || "checked".equals(attrName);
    }
    
    private String escapeAttribute(String value) {
        // Similar to escapeURI but for regular attributes
        return value.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;");
    }
    
    private String escapeURI(String uri) {
        // URI-specific escaping per policy
        return escapeAttribute(uri);
    }
    
    private boolean shouldEncodeChar(char c) {
        // Check against policy's international character encoding rules
        return Character.isHighSurrogate(c) || 
               (c > 127 && policy.isEntityEncodeIntlCharacters());
    }
}