package org.nutz.aop;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.nutz.ioc.Ioc;
import org.nutz.ioc.aop.Aop;
import org.nutz.lang.Lang;
import org.nutz.lang.Mirror;

/**
 * 提供ClassAgent的基础实现,拦截不可能插入Aop代码的Class
 * <p/>传入的Class对象需要满足的条件
 * <li>不能是final或者abstract的
 * <li>必须有非private的构造函数
 * </p>被拦截的方法需要满足的条件
 * <li>不能是final或者abstract的
 * <li>不是private的
 * @author wendal(wendal1985@gmail.com)
 *
 */
public abstract class AbstractClassAgent implements ClassAgent {

	private ArrayList<Pair> pairs = new ArrayList<Pair>();

	public ClassAgent addListener(MethodMatcher matcher, MethodInterceptor listener) {
		if (null != listener)
			pairs.add(new Pair(matcher, listener));
		return this;
	}

	public <T> Class<T> define(Class<T> klass) {
		if (checkClass(klass) == false)
			return klass;
		Pair2[] pair2s = findMatchedMethod(klass);
		if (pair2s == null)
			return klass;
		String newName = klass.getName() + CLASSNAME_SUFFIX;
		Class<T> newClass = try2Load(newName);
		if(newClass != null)
			return newClass;
		Constructor<T> [] constructors = getEffectiveConstructors(klass);
		newClass = generate(pair2s, newName, klass,constructors);
		return newClass;
	}
	
	protected Ioc ioc;
	
	public void setIoc(Ioc ioc) {
		this.ioc = ioc;
	}
	
	protected abstract <T> Class<T> generate(Pair2 [] pair2s,String newName,Class<T> klass,Constructor<T> [] constructors) ;
	
	@SuppressWarnings("unchecked")
	protected <T> Constructor<T> [] getEffectiveConstructors(Class<T> klass) {
		Constructor<T> [] constructors = (Constructor<T>[]) klass.getDeclaredConstructors();
		List<Constructor<T>> cList = new ArrayList<Constructor<T>>();
		for (int i = 0; i < constructors.length; i++) {
			Constructor<T> constructor = constructors[i];
			if(Modifier.isPrivate(constructor.getModifiers()))
				continue;
			cList.add(constructor);
		}
		if(cList.size() == 0)
			throw Lang.makeThrow("没有找到任何非private的构造方法,无法创建子类!");
		return cList.toArray(new Constructor[cList.size()]);
	}
	
	protected <T> boolean checkClass(Class<T> klass) {
		if (klass == null)
			return false;
		String klass_name = klass.getName();
		if (klass_name.endsWith(CLASSNAME_SUFFIX))
			return false;
		if (klass.isInterface() || klass.isArray() 
				|| klass.isEnum() || klass.isPrimitive() 
				|| klass.isMemberClass())
			throw Lang.makeThrow("需要拦截的%s不是一个顶层类!创建失败!", klass_name);
		if (Modifier.isFinal(klass.getModifiers()) || Modifier.isAbstract(klass.getModifiers()))
			return false;
		return true;
	}
	
	@SuppressWarnings("unchecked")
	protected <T> Class<T> try2Load(String newName) {
		ClassLoader classLoader = getClass().getClassLoader();
		try {
			return (Class<T>) Class.forName(newName, false, classLoader);
		} catch (ClassNotFoundException e2) {
			try {
				return (Class<T>) Class.forName(newName);
			} catch (ClassNotFoundException e1) {
				try {
					return (Class<T>) classLoader.loadClass(newName);
				} catch (ClassNotFoundException e) {
				}
			}
		}
		return null;
	}
	
	private <T> Pair2[] findMatchedMethod(Class<T> klass) {
		Method[] all = Mirror.me(klass).getAllDeclaredMethodsWithoutTop();
		List<Pair2> p2 = new ArrayList<Pair2>();
		for (Method m : all) {
			int mod = m.getModifiers();
			if (mod == 0 || Modifier.isStatic(mod) || Modifier.isPrivate(mod))
				continue;
			ArrayList<MethodInterceptor> mls = new ArrayList<MethodInterceptor>();
			Aop aop = m.getAnnotation(Aop.class);
			if(ioc != null &&aop != null){
				String [] values = aop.value();
				if(values.length > 0){
					for (String methodInterceptorName : values){
						MethodInterceptor interceptor = ioc.get(MethodInterceptor.class, methodInterceptorName);
						if(interceptor != null && ! mls.contains(interceptor))
							mls.add(interceptor);
					}
				}
			}
			for (Pair pair : pairs)
				if(pair.matcher.match(m) && ! mls.contains(pair.interceptor))
					mls.add(pair.interceptor);
			if(mls.size() > 0 )
				p2.add(new Pair2(m,mls));
		}
		if(p2.size() > 0)
			return p2.toArray(new Pair2[p2.size()]);
		return null;
	}
	
	protected static class Pair {
		Pair(MethodMatcher matcher, MethodInterceptor interceptor) {
			this.matcher = matcher;
			this.interceptor = interceptor;
		}

		MethodMatcher matcher;
		MethodInterceptor interceptor;
	}

	protected static class Pair2 {
		Pair2(Method method, ArrayList<MethodInterceptor> interceptors) {
			this.method = method;
			this.interceptors = interceptors;
		}

		public Method method;
		public ArrayList<MethodInterceptor> interceptors;
	}
}
