package org.nutz.el.obj;

import org.nutz.el.ElObj;
import org.nutz.el.ElValue;
import org.nutz.lang.Lang;
import org.nutz.lang.util.Context;

public class ArrayObj implements ElObj {

	private ElObj[] objs;

	public ArrayObj(ElObj[] objs) {
		this.objs = null == objs ? new ElObj[0] : objs;
	}

	public ElValue eval(Context context) {
		throw Lang.noImplement();
	}

	public ElValue[] evalArray(Context context) {
		ElValue[] vals = new ElValue[objs.length];
		for (int i = 0; i < vals.length; i++) {
			vals[i] = objs[i].eval(context);
		}
		return vals;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append('[');
		if (objs.length > 0) {
			sb.append(objs[0].toString());
			for (int i = 1; i < objs.length; i++) {
				sb.append(", ").append(objs[i].toString());
			}
		}
		return sb.append(']').toString();
	}

}
