package com.shopstyle.snout;

import java.util.EnumSet;

import org.apache.commons.lang3.StringUtils;

public enum MetaRobots {

	Index,
	NoIndex,
	Follow,
	NoFollow;

	public static EnumSet<MetaRobots> parse(String s){
		EnumSet<MetaRobots> set = EnumSet.noneOf(MetaRobots.class);
		if (StringUtils.isBlank(s)){
			return set;
		}
		String[] parts = s.split(",");
		for (String part : parts){
			part = part.trim();
			for (MetaRobots enumVal : MetaRobots.values()){
				String enumStrVal = enumVal.name();
				if (enumStrVal.equalsIgnoreCase(part)){
					set.add(enumVal);
				}
			}
		}
		return set;
	}

}
