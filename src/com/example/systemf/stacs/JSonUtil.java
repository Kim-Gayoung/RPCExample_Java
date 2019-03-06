package com.example.systemf.stacs;

import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.example.systemf.ast.Type;
import com.example.systemf.ast.VarType;
import com.example.systemf.sta.ast.Bool;
import com.example.systemf.sta.ast.Clo;
import com.example.systemf.sta.ast.Num;
import com.example.systemf.sta.ast.Str;
import com.example.systemf.sta.ast.Unit;
import com.example.systemf.sta.ast.Value;

public class JSonUtil {
	public static Value fromJson (JSONObject json) {
		Object obj;
		
		obj = json.get(Unit.Unit);
		if (obj instanceof String) {
			return new Unit();
		}
		
		obj = json.get(Bool.Bool);
		if (obj instanceof Boolean) {
			Boolean b = (Boolean) obj;
			return new Bool(b.toString());
		}
		
		obj = json.get(Num.Num);
		if (obj instanceof Long) {	// Integers in JSON is represented by Long class in Java
			Long i = (Long)obj;
			return new Num(i.intValue());
		}
		
		obj = json.get(Str.Str);
		if (obj instanceof String) {
			String s = (String) obj;
			return new Str(s);
		}
		
		obj = json.get(Clo.Clo);
		if (obj instanceof String) {
			String f = (String)obj;
			JSONArray jsonArr = (JSONArray) json.get(Clo.Fvs);
			JSONArray typeArr = (JSONArray) json.get(Clo.Tvs);
			
			ArrayList<Type> tys = new ArrayList<>();
			for (int i = 0; i < typeArr.size(); i++) {
				Type ty = JSonUtil.typeFromJson((JSONObject) typeArr.get(i));
				tys.add(ty);
			}
			
			ArrayList<Value> args = new ArrayList<Value>();
			for (int i=0; i<jsonArr.size(); i++) {
				Value arg = JSonUtil.fromJson((JSONObject)jsonArr.get(i));
				args.add(arg);
			}
			return new Clo(f, tys, args);
		}
		
		System.err.println("JSonUtil: fromJson: Neither Const or Clo\n" + json);
		
		return null; // Must not reach here.
	}

	public static Type typeFromJson(JSONObject json) {
		Object obj;
		
		obj = json.get(VarType.VarType);
		if (obj instanceof String) {
			String s = (String) obj;
			
			return new VarType(s);
		}
		
		System.err.println("JSonUtil: typeFromJson: Not VarType.\n" + json);
		
		return null;
	}
}
