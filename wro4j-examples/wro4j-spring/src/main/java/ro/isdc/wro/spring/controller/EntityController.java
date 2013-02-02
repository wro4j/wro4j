package ro.isdc.wro.spring.controller;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import ro.isdc.wro.spring.model.MyEntity;
import ro.isdc.wro.spring.service.EntityService;


/**
 * Handles requests for the application home page.
 */
@Controller
public class EntityController {

	private static final Logger logger = LoggerFactory.getLogger(EntityController.class);

	@Inject
	EntityService entityService;

	@ModelAttribute("entity")
	public MyEntity init(@PathVariable final String id) {
		return entityService.findEntity(id);
	}

	@RequestMapping(value="/myentity/{id}", method=RequestMethod.GET)
	public ModelAndView view(final ModelAndView mv, @ModelAttribute("entity") final MyEntity entity) {
		mv.addObject("entity", entity);
		mv.setViewName("entity_detail");
		logger.info("requesting /myentity");
		return mv;
	}

	@RequestMapping(value="/myentity/{id}", method=RequestMethod.POST)
	public String update(final ModelAndView mv, @ModelAttribute("entity") final MyEntity entity) {
	    	logger.info("updating /myentity");
		return "redirect:/myentity/"+entityService.save(entity).getId();
	}
}

