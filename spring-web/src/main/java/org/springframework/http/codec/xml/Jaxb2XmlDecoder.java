/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.http.codec.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.XMLEvent;

import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.core.ResolvableType;
import org.springframework.core.codec.AbstractDecoder;
import org.springframework.core.codec.CodecException;
import org.springframework.core.codec.DecodingException;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.util.xml.StaxUtils;

/**
 * Decode from a bytes stream containing XML elements to a stream of
 * {@code Object}s (POJOs).
 *
 * @author Sebastien Deleuze
 * @author Arjen Poutsma
 * @see Jaxb2XmlEncoder
 * @since 5.0
 */
public class Jaxb2XmlDecoder extends AbstractDecoder<Object> {

	/**
	 * The default value for JAXB annotations.
	 *
	 * @see XmlRootElement#name()
	 * @see XmlRootElement#namespace()
	 * @see XmlType#name()
	 * @see XmlType#namespace()
	 */
	private static final String JAXB_DEFAULT_ANNOTATION_VALUE = "##default";


	private final XmlEventDecoder xmlEventDecoder = new XmlEventDecoder();

	private final JaxbContextContainer jaxbContexts = new JaxbContextContainer();


	public Jaxb2XmlDecoder() {
		super(MimeTypeUtils.APPLICATION_XML, MimeTypeUtils.TEXT_XML);
	}


	@Override
	public boolean canDecode(ResolvableType elementType, @Nullable MimeType mimeType) {
		Class<?> outputClass = elementType.getRawClass();
		return (outputClass != null && (outputClass.isAnnotationPresent(XmlRootElement.class) ||
				outputClass.isAnnotationPresent(XmlType.class)) && super.canDecode(elementType, mimeType));
	}

	@Override
	public Flux<Object> decode(Publisher<DataBuffer> inputStream, ResolvableType elementType,
							   @Nullable MimeType mimeType, @Nullable Map<String, Object> hints) {

		Class<?> outputClass = elementType.getRawClass();
		Assert.state(outputClass != null, "Unresolvable output class");

		Flux<XMLEvent> xmlEventFlux = this.xmlEventDecoder.decode(
				inputStream, ResolvableType.forClass(XMLEvent.class), mimeType, hints);

		QName typeName = toQName(outputClass);
		Flux<List<XMLEvent>> splitEvents = split(xmlEventFlux, typeName);

		return splitEvents.map(events -> unmarshal(events, outputClass));
	}

	@Override
	public Mono<Object> decodeToMono(Publisher<DataBuffer> inputStream, ResolvableType elementType,
									 @Nullable MimeType mimeType, @Nullable Map<String, Object> hints) {

		return decode(inputStream, elementType, mimeType, hints).singleOrEmpty();
	}

	private Object unmarshal(List<XMLEvent> events, Class<?> outputClass) {
		try {
			Unmarshaller unmarshaller = this.jaxbContexts.createUnmarshaller(outputClass);
			XMLEventReader eventReader = StaxUtils.createXMLEventReader(events);
			if (outputClass.isAnnotationPresent(XmlRootElement.class)) {
				return unmarshaller.unmarshal(eventReader);
			} else {
				JAXBElement<?> jaxbElement = unmarshaller.unmarshal(eventReader, outputClass);
				return jaxbElement.getValue();
			}
		} catch (UnmarshalException ex) {
			throw new DecodingException("Could not unmarshal XML to " + outputClass, ex);
		} catch (JAXBException ex) {
			throw new CodecException("Invalid JAXB configuration", ex);
		}
	}

	/**
	 * Returns the qualified name for the given class, according to the mapping rules
	 * in the JAXB specification.
	 */
	QName toQName(Class<?> outputClass) {
		String localPart;
		String namespaceUri;

		if (outputClass.isAnnotationPresent(XmlRootElement.class)) {
			XmlRootElement annotation = outputClass.getAnnotation(XmlRootElement.class);
			localPart = annotation.name();
			namespaceUri = annotation.namespace();
		} else if (outputClass.isAnnotationPresent(XmlType.class)) {
			XmlType annotation = outputClass.getAnnotation(XmlType.class);
			localPart = annotation.name();
			namespaceUri = annotation.namespace();
		} else {
			throw new IllegalArgumentException("Output class [" + outputClass.getName() +
					"] is neither annotated with @XmlRootElement nor @XmlType");
		}

		if (JAXB_DEFAULT_ANNOTATION_VALUE.equals(localPart)) {
			localPart = ClassUtils.getShortNameAsProperty(outputClass);
		}
		if (JAXB_DEFAULT_ANNOTATION_VALUE.equals(namespaceUri)) {
			Package outputClassPackage = outputClass.getPackage();
			if (outputClassPackage != null && outputClassPackage.isAnnotationPresent(XmlSchema.class)) {
				XmlSchema annotation = outputClassPackage.getAnnotation(XmlSchema.class);
				namespaceUri = annotation.namespace();
			} else {
				namespaceUri = XMLConstants.NULL_NS_URI;
			}
		}
		return new QName(namespaceUri, localPart);
	}

	/**
	 * Split a flux of {@link XMLEvent}s into a flux of XMLEvent lists, one list
	 * for each branch of the tree that starts with the given qualified name.
	 * That is, given the XMLEvents shown {@linkplain XmlEventDecoder here},
	 * and the {@code desiredName} "{@code child}", this method returns a flux
	 * of two lists, each of which containing the events of a particular branch
	 * of the tree that starts with "{@code child}".
	 * <ol>
	 * <li>The first list, dealing with the first branch of the tree:
	 * <ol>
	 * <li>{@link javax.xml.stream.events.StartElement} {@code child}</li>
	 * <li>{@link javax.xml.stream.events.Characters} {@code foo}</li>
	 * <li>{@link javax.xml.stream.events.EndElement} {@code child}</li>
	 * </ol>
	 * <li>The second list, dealing with the second branch of the tree:
	 * <ol>
	 * <li>{@link javax.xml.stream.events.StartElement} {@code child}</li>
	 * <li>{@link javax.xml.stream.events.Characters} {@code bar}</li>
	 * <li>{@link javax.xml.stream.events.EndElement} {@code child}</li>
	 * </ol>
	 * </li>
	 * </ol>
	 */
	Flux<List<XMLEvent>> split(Flux<XMLEvent> xmlEventFlux, QName desiredName) {
		return xmlEventFlux.flatMap(new SplitFunction(desiredName));
	}


	private static class SplitFunction implements Function<XMLEvent, Publisher<? extends List<XMLEvent>>> {

		private final QName desiredName;

		@Nullable
		private List<XMLEvent> events;

		private int elementDepth = 0;

		private int barrier = Integer.MAX_VALUE;

		public SplitFunction(QName desiredName) {
			this.desiredName = desiredName;
		}

		@Override
		public Publisher<? extends List<XMLEvent>> apply(XMLEvent event) {
			if (event.isStartElement()) {
				if (this.barrier == Integer.MAX_VALUE) {
					QName startElementName = event.asStartElement().getName();
					if (this.desiredName.equals(startElementName)) {
						this.events = new ArrayList<>();
						this.barrier = this.elementDepth;
					}
				}
				this.elementDepth++;
			}
			if (this.elementDepth > this.barrier) {
				Assert.state(this.events != null, "No XMLEvent List");
				this.events.add(event);
			}
			if (event.isEndElement()) {
				this.elementDepth--;
				if (this.elementDepth == this.barrier) {
					this.barrier = Integer.MAX_VALUE;
					Assert.state(this.events != null, "No XMLEvent List");
					return Mono.just(this.events);
				}
			}
			return Mono.empty();
		}
	}

}
