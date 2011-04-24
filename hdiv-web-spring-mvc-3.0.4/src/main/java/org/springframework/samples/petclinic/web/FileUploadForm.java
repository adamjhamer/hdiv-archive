package org.springframework.samples.petclinic.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/upload")
public class FileUploadForm {

	private static final Logger logger = LoggerFactory.getLogger(FileUploadForm.class);

	@RequestMapping(method = RequestMethod.GET)
	public String setupForm(Model model) {

		return "upload/form";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String processSubmit(@RequestParam("file") MultipartFile file) {
		if (file == null) {
			return "upload/form";
		} else {

			logger.info("File name: " + file.getName());
			logger.info("File size: " + file.getSize());

			return "redirect:/";
		}
	}

}
