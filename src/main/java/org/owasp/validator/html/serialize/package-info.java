/**
 * Provides classes for HTML serialization within the AntiSamy project.
 * <p>
 * The classes in this package implement modern SAX-based content handlers
 * that replace legacy HTML serialization approaches. They enforce AntiSamy
 * policy rules during output generation, ensuring that HTML is written in a
 * safe, standards-compliant way.
 * </p>
 *
 * <h2>Key Features</h2>
 * <ul>
 *   <li>Serialization of HTML elements using SAX {@code ContentHandler} APIs.</li>
 *   <li>Support for void elements (e.g., {@code <br>}, {@code <img>}) without
 *       self-closing syntax, consistent with HTML5.</li>
 *   <li>Automatic escaping of attributes and text content to prevent injection
 *       or unsafe markup.</li>
 *   <li>Policy-driven handling of empty tags, required closing tags, and
 *       comment preservation.</li>
 *   <li>Integration with AntiSamy {@link org.owasp.validator.html.InternalPolicy}
 *       and {@link org.owasp.validator.html.TagMatcher} for fine-grained control.</li>
 * </ul>
 *
 * <h2>Usage</h2>
 * <p>
 * Typical usage involves constructing a {@link org.owasp.validator.html.serialize.HTMLContentHandler}
 * with a {@link java.io.Writer} and an AntiSamy policy. The handler can then be
 * attached to a SAX parser to serialize sanitized HTML output.
 * </p>
 *
 * <h2>Package Contents</h2>
 * <ul>
 *   <li>{@link org.owasp.validator.html.serialize.HTMLContentHandler} – Main
 *       implementation of a SAX {@code ContentHandler} for HTML serialization.</li>
 *   <!-- Future serializer classes can be documented here -->
 * </ul>
 *
 * @author  
 *   OWASP AntiSamy Project
 * @see org.owasp.validator.html.InternalPolicy
 * @see org.owasp.validator.html.TagMatcher
 */
package org.owasp.validator.html.serialize;
