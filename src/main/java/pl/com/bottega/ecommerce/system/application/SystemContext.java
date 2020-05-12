package pl.com.bottega.ecommerce.system.application;

import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.Id;

public class SystemContext {
	public SystemUser getSystemUser(){
		return new SystemUser(new Id("1"));//TODO introduce security integration
	}
}
