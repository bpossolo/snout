package com.shopstyle.snout;

import org.jsoup.nodes.Element;

public enum ElementType {
	Canonical,
	Hreflang,
	H1,
	Title,
	Robots;

	private static final String HrefAttr = "href";
	private static final String ContentAttr = "content";

	public Object getElementValue(Element element){
		switch (this){
			case Robots:
				String value = element.attr(ContentAttr);
				if (value == null){
					return null;
				}
				else {
					return MetaRobots.parse(value);
				}
			case H1:
			case Title:
				return element.text();
			case Canonical:
			case Hreflang:
				return element.attr(HrefAttr);
		}
		return null;
	}
}
