package com.example.systemf.sta.ast;

import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.example.systemf.ast.Type;

public class Clo extends Value {
	private String f;
	private ArrayList<Type> ts;
	private ArrayList<Value> vs;

	public Clo(String f, ArrayList<Type> ts, ArrayList<Value> vs) {
		this.f = f;
		this.ts = ts;
		this.vs = vs;
	}

	public String getF() {
		return f;
	}

	public void setF(String f) {
		this.f = f;
	}

	public ArrayList<Value> getVs() {
		return vs;
	}

	public void setVs(ArrayList<Value> vs) {
		this.vs = vs;
	}
	
	public ArrayList<Type> getTs() {
		return ts;
	}

	public void setTs(ArrayList<Type> ts) {
		this.ts = ts;
	}

	@Override
	public String toString() {
		String ret = "Clo(" + f + ", {";
		int cnt = 0;
		
		for (Type t : ts) {
			if (t == null) 
				System.err.println("Clo " + f + " has something null type inside");
			ret += t.toString();
			
			if (cnt != ts.size() - 1) {
				ret += " ";
				cnt++;
			}
		}
		
		ret += "}, {";
		cnt = 0;
		
		for (Value v : vs) {
			if (v == null) 
				System.err.println("Clo " + f + " has something null inside");
			ret += v.toString();
			
			if (cnt != vs.size() - 1) {
				ret += " ";
				cnt++;
			}
		}
		ret += "})";
		
		return ret;
	}
	
	public static final String Clo = "Clo";
	public static final String Fvs = "Fvs";
	
	@Override
	@SuppressWarnings("unchecked")
	public JSONObject toJson() {
		JSONObject jsonObject = new JSONObject();
		
		// Function name
		jsonObject.put(Clo, f);
		
		// Free variables
		JSONArray jsonArray = new JSONArray();
		for(Value sv : vs) {
			jsonArray.add(sv.toJson());
		}
		
		jsonObject.put(Fvs, jsonArray);
	
		return jsonObject;
	}
}
