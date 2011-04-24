package org.hdiv.components;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.component.UIParameter;
import javax.faces.component.html.HtmlOutcomeTargetButton;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hdiv.config.HDIVConfig;
import org.hdiv.dataComposer.IDataComposer;
import org.hdiv.helper.OutcomeTargetComponentHelper;
import org.hdiv.util.Constants;
import org.hdiv.util.HDIVUtil;
import org.hdiv.util.RequestUtilsHDIV;

/**
 * Extends HtmlOutcomeTargetButton in order to secure component
 * 
 * @author Gotzon Illarramendi
 * 
 */
public class HtmlOutcomeTargetButtonExtension extends HtmlOutcomeTargetButton {

	private static Log log = LogFactory.getLog(HtmlOutcomeTargetButtonExtension.class);

	/**
	 * Helper for creating component's url
	 */
	private OutcomeTargetComponentHelper helper = new OutcomeTargetComponentHelper();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.faces.component.UIComponentBase#encodeBegin(javax.faces.context
	 * .FacesContext)
	 */
	public void encodeBegin(FacesContext context) throws IOException {

		if (log.isDebugEnabled()) {
			log.debug("encodeBegin");
		}

		try {
			ExternalContext externalContext = context.getExternalContext();
			HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();

			String url = this.helper.getUrl(context, this);
			
			// Component creates internal links, so HDIV state must always be created

			HDIVConfig hdivConfig = HDIVUtil.getHDIVConfig(request.getSession().getServletContext());

			// if url has not got parameters, we do not have to include HDIV's
			// state

			if (!hdivConfig.isValidationInUrlsWithoutParamsActivated() && !url.contains("?")
					&& !this.helper.hasUIParamChilds(this)) {

				super.encodeBegin(context);
				return;
			}

			// Check if url points to a resource that doesn't need to be secured,
			// as an image
			if (RequestUtilsHDIV.isResourceUrl(hdivConfig, url)) {
				if (log.isDebugEnabled()) {
					log.debug("is resource url");
				}
				super.encodeBegin(context);
				return;
			}

			// Get DataComposer object from request
			IDataComposer dataComposer = HDIVUtil.getDataComposer(request);

			// Confidentiality is disabled, so url doesn't change
			String encodedUrl = RequestUtilsHDIV.composeURL(request, dataComposer, url);

			// HDIV's state id, just created
			String requestId = dataComposer.endRequest();

			String hdivParameter = (String) externalContext.getSessionMap().get(Constants.HDIV_PARAMETER);

			// Add a children UIParam component with HDIV state
			UIParameter paramComponent = (UIParameter) context.getApplication().createComponent(
					UIParameter.COMPONENT_TYPE);
			paramComponent.setName(hdivParameter);
			paramComponent.setValue(requestId);
			this.getChildren().add(paramComponent);

		} catch (FacesException e) {
			log.error("Error en HtmlOutputLinkExtension: " + e.getMessage());
			throw e;
		}

		super.encodeBegin(context);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.faces.component.UIComponentBase#encodeEnd(javax.faces.context.
	 * FacesContext)
	 */
	public void encodeEnd(FacesContext context) throws IOException {

		super.encodeEnd(context);
		
		// Remove the component with the HDIV state, we don't want to store
		// it in the state
		String hdivParameter = (String) context.getExternalContext().getSessionMap().get(Constants.HDIV_PARAMETER);

		Iterator it = this.getChildren().iterator();
		List<Integer> toRemoveList = new ArrayList<Integer>();
		while (it.hasNext()) {
			UIComponent comp = (UIComponent) it.next();
			if (comp instanceof UIParameter) {
				UIParameter param = (UIParameter) comp;
				String name = param.getName();
				if (name != null && name.equals(hdivParameter)) {
					Integer index = this.getChildren().indexOf(param);
					toRemoveList.add(index);
				}
			}
		}
		for (Integer index : toRemoveList) {
			this.getChildren().remove(index.intValue());
		}
	}

}
