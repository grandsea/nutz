package org.nutz.el2.obj;

import org.nutz.lang.util.Context;

/**
 * 标识符对象
 * @author juqkai(juqkai@gmail.com)
 *
 */
public class IdentifierObj {
	private String val;
	private Context context;
	public IdentifierObj(String val) {
		this.val = val;
	}
	public String getVal() {
		return val;
	}
	public Object fetchVal(){
		if(context.has(val)){
			return context.get(val);
		}
		throw new RuntimeException("找不到变量'"+val+"'!");
	}
	public String toString() {
		return val;
	}
	public void setContext(Context context) {
		this.context = context;
	}
}