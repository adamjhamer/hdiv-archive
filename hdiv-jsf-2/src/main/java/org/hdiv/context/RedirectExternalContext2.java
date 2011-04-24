package org.hdiv.context;

import java.io.IOException;

import javax.faces.context.ExternalContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Wrapper of ExternalContext.
 * 
 * Generates HDIV's state for redirects and adds it to the url as a
 * parameter
 * 
 * @author Gotzon Illarramendi
 * 
 */
public class RedirectExternalContext2 extends javax.faces.context.ExternalContextWrapper {

	private static Log log = LogFactory.getLog(RedirectExternalContext2.class);

	/**
	 * Class for helping with the redirect logic
	 */
	private RedirectHelper redirectHelper = new RedirectHelper();

	/**
	 * Original ExternalContext
	 */
	private ExternalContext wrapped;

	/**
	 * Default constructor
	 * 
	 * @param wrapped
	 *            original ExternalContext
	 */
	public RedirectExternalContext2(ExternalContext wrapped) {
		super();
		this.wrapped = wrapped;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.faces.context.ExternalContextWrapper#getWrapped()
	 */
	public ExternalContext getWrapped() {

		return this.wrapped;
	}

	/**
	 * If it is an internal redirect (to the application itself) generates
	 * the state, stores it in session and adds corresponding parameter
	 * to url.
	 */
	public void redirect(String url) throws IOException {

		// Add state to url
		String finalUrl = this.redirectHelper.addHDIVStateToURL(url);
		if (log.isDebugEnabled()) {
			log.debug("redireccionando a la url:" + finalUrl);
		}

		this.wrapped.redirect(finalUrl);

	}

}
