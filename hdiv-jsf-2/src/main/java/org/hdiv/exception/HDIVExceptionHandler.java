package org.hdiv.exception;

import java.util.Iterator;

import javax.faces.FacesException;
import javax.faces.application.NavigationHandler;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerWrapper;
import javax.faces.context.FacesContext;
import javax.faces.event.ExceptionQueuedEvent;
import javax.faces.event.ExceptionQueuedEventContext;
import javax.servlet.ServletContext;

import org.hdiv.config.HDIVConfig;
import org.hdiv.util.HDIVUtil;

/**
 * ExceptionHandler that processes HDIV exceptions
 * 
 * @author Gotzon Illarramendi
 */
public class HDIVExceptionHandler extends ExceptionHandlerWrapper {

	/**
	 * Original ExceptionHandler
	 */
	private ExceptionHandler original;

	/**
	 * Constructor
	 * 
	 * @param original
	 *            original ExceptionHandler
	 */
	public HDIVExceptionHandler(ExceptionHandler original) {
		this.original = original;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.faces.context.ExceptionHandlerWrapper#getWrapped()
	 */
	public ExceptionHandler getWrapped() {

		return this.original;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.faces.context.ExceptionHandlerWrapper#handle()
	 */
	public void handle() throws FacesException {

		for (Iterator i = super.getUnhandledExceptionQueuedEvents().iterator(); i.hasNext();) {
			ExceptionQueuedEvent event = (ExceptionQueuedEvent) i.next();
			ExceptionQueuedEventContext context = (ExceptionQueuedEventContext) event.getSource();
			Throwable t = context.getException();
			Throwable cause = this.getRootCause(t);
			if (cause instanceof StateValidationException) {
				StateValidationException hdivExc = (StateValidationException) cause;
				try {

					FacesContext fc = FacesContext.getCurrentInstance();
					NavigationHandler nav = fc.getApplication().getNavigationHandler();
					nav.handleNavigation(fc, null, this.getErrorPage(fc));
					fc.renderResponse();

				} finally {
					i.remove();
				}
			}
		}
		getWrapped().handle();
	}

	/**
	 * Obtains error page from HDIV configuration.
	 * 
	 * @param facesContext
	 *            active FacesContext 
	 * @return error page
	 */
	private String getErrorPage(FacesContext facesContext) {

		ServletContext servletContext = (ServletContext) facesContext.getExternalContext().getContext();
		HDIVConfig config = HDIVUtil.getHDIVConfig(servletContext);

		String errorPage = config.getErrorPage();
		return errorPage;

	}
}
