package org.nutz.el.impl.normalizer;

import org.nutz.el.El;
import org.nutz.el.ElObj;
import org.nutz.el.impl.SymbolNormalizer;
import org.nutz.el.impl.SymbolNormalizing;

public class StringNormalizer implements SymbolNormalizer {

	public ElObj normalize(SymbolNormalizing ing) {
		return El.Obj.string(ing.current().getString());
	}

}
