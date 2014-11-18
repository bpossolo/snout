package com.shopstyle.snout;

import java.text.MessageFormat;
import java.util.Collection;

import org.apache.commons.lang3.StringUtils;


public class Failure {

	private static final String EmptyBrackets = "[]";
	private static final String IOFailure = "Failed to HTTP GET {0}";
	private static final String ComparisonFailure = "Expected {0} to be [{1}] but found [{2}]";
	private static final String InsufficientElementsFailure = "Expected 1 {0} with value [{1}] but found 0";
	private static final String UnexpectedElementsFailure = "Did not expect {0} but found [{1}]";
	private static final String TooManyElementsFailure = "Expected 1 {0} with value [{1}] but found multiple [{2}]";

	private ElementType type;
	private String msg;

	public Failure(String msg){
		this.msg = msg;
	}

	public Failure(ElementType type, String msg){
		this.type = type;
		this.msg = msg;
	}

	public ElementType getType() {
		return type;
	}

	public String getMsg() {
		return msg;
	}

	public boolean isIOFailure() {
		return type == null;
	}

	public static Failure io(String url){
		String msg = MessageFormat.format(IOFailure, url);
		return new Failure(msg);
	}

	public static Failure comparison(Object expected, Object actual, ElementType type){
		expected = prepForLogging(expected);
		actual = prepForLogging(actual);
		String msg = MessageFormat.format(ComparisonFailure, type, expected, actual);
		return new Failure(type, msg);
	}

	public static Failure insufficientElements(Object expected, ElementType type){
		expected = prepForLogging(expected);
		String msg = MessageFormat.format(InsufficientElementsFailure, type, expected);
		return new Failure(type, msg);
	}

	public static Failure unexpectedElements(Object actual, ElementType type){
		actual = prepForLogging(actual);
		String msg = MessageFormat.format(UnexpectedElementsFailure, type, actual);
		return new Failure(type, msg);
	}

	public static Failure tooManyElements(Object expected, Object actual, ElementType type){
		expected = prepForLogging(expected);
		actual = prepForLogging(actual);
		String msg = MessageFormat.format(TooManyElementsFailure, type, expected, actual);
		return new Failure(type, msg);
	}

	private static Object prepForLogging(Object obj){
		if (obj instanceof Collection){
			// strip the surrounding brackets so we don't see them twice in the failure message
			String s = obj.toString();
			if (EmptyBrackets.equals(s)){
				return StringUtils.EMPTY;
			}
			else {
				return s.subSequence(1, s.length() - 1);
			}
		}
		return obj;
	}

}
