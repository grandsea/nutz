package org.nutz.el2.opt;

import org.nutz.el2.Operator;
import org.nutz.el2.obj.IdentifierObj;

public abstract class AbstractOpt implements Operator{
	/**
	 * 操作符对象自身的符号
	 * @return
	 */
	public abstract OptEnum fetchSelf();
	public boolean equals(Object obj) {
		if(obj.equals(fetchSelf())){
			return true;
		}
		return super.equals(obj);
	}
	public String toString() {
		return String.valueOf(fetchSelf());
	}
	
	/**
	 * 计算子项
	 * @param obj
	 * @return
	 */
	protected Object calculateItem(Object obj){
		if(obj instanceof Number){
			return obj;
		}
		if(obj instanceof Boolean){
			return obj;
		}
		if(obj instanceof IdentifierObj){
			return ((IdentifierObj) obj).fetchVal();
		}
		if(obj instanceof Operator){
			return ((Operator) obj).calculate();
		}
		throw new RuntimeException("未知计算类型!" + obj);
		
	}
}