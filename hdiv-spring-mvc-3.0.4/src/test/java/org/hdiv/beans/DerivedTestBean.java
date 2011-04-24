package org.hdiv.beans;

import java.io.Serializable;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;

/**
 * @author Juergen Hoeller
 * @since 21.08.2003
 */
public class DerivedTestBean extends TestBean implements Serializable, BeanNameAware, DisposableBean {

	private String beanName;

	private boolean initialized;

	private boolean destroyed;


	public DerivedTestBean() {
	}

	public DerivedTestBean(String[] names) {
		if (names == null || names.length < 2) {
			throw new IllegalArgumentException("Invalid names array");
		}
		setName(names[0]);
		setBeanName(names[1]);
	}

	public static DerivedTestBean create(String[] names) {
		return new DerivedTestBean(names);
	}


	public void setBeanName(String beanName) {
		if (this.beanName == null || beanName == null) {
			this.beanName = beanName;
		}
	}

	public String getBeanName() {
		return beanName;
	}

	public void setSpouseRef(String name) {
		setSpouse(new TestBean(name));
	}


	public void initialize() {
		this.initialized = true;
	}

	public boolean wasInitialized() {
		return initialized;
	}


	public void destroy() {
		this.destroyed = true;
	}

	public boolean wasDestroyed() {
		return destroyed;
	}

}