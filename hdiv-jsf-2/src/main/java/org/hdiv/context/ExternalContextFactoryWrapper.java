package org.hdiv.context;

import javax.faces.FacesException;
import javax.faces.context.ExternalContext;
import javax.faces.context.ExternalContextFactory;

/**
 * Factory to create HDIV's ExternalContext objects instead of JSF's base 
 * implementation 
 * 
 * @author Gotzon Illarramendi
 * 
 */
public class ExternalContextFactoryWrapper extends ExternalContextFactory {

	/**
	 * Original ExternalContextFactory
	 */
	private ExternalContextFactory original;

	/**
	 * Default constructor
	 * 
	 * @param original
	 *            original ExternalContextFactory
	 */
	public ExternalContextFactoryWrapper(ExternalContextFactory original) {
		super();
		this.original = original;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.faces.context.ExternalContextFactory#getWrapped()
	 */
	public ExternalContextFactory getWrapped() {

		return this.original;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.faces.context.ExternalContextFactory#getExternalContext(java.lang
	 * .Object, java.lang.Object, java.lang.Object)
	 */
	public ExternalContext getExternalContext(Object context, Object request, Object response) throws FacesException {

		ExternalContext ec = this.original.getExternalContext(context, request, response);

		return new RedirectExternalContext2(ec);
	}

}
