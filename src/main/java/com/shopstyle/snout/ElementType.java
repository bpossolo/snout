package com.shopstyle.snout;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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

	@SuppressWarnings("unchecked")
	public List<Object> getElementValues(Elements elements){
		List<Object> list = new ArrayList<>(elements.size());
		for (int i = 0; i < elements.size(); i++){
			Object value = getElementValue(elements.get(i));
			if (this == Robots){
				EnumSet<MetaRobots> set = (EnumSet<MetaRobots>)value;
				list.addAll(set);
			}
			else {
				list.add(value);
			}
		}
		return list;
	}
}
