package com.example.systemf.ast;

import org.json.simple.JSONObject;

public abstract class Type {
	@Override
	protected abstract Type clone();
	public JSONObject toJson() {
		return new JSONObject();
	}
}
