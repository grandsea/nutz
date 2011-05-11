package org.nutz.dao.impl.jdbc.hsqldb;

import java.util.List;

import org.nutz.dao.DB;
import org.nutz.dao.Dao;
import org.nutz.dao.Sqls;
import org.nutz.dao.entity.Entity;
import org.nutz.dao.entity.MappingField;
import org.nutz.dao.entity.PkType;
import org.nutz.dao.impl.jdbc.AbstractJdbcExpert;
import org.nutz.dao.jdbc.JdbcExpertConfigFile;
import org.nutz.dao.pager.Pager;
import org.nutz.dao.sql.Pojo;
import org.nutz.dao.util.Pojos;

/**
 * 
 * @author wendal
 */
public class HsqldbJdbcExpert extends AbstractJdbcExpert {

	public HsqldbJdbcExpert(JdbcExpertConfigFile conf) {
		super(conf);
	}

	public String getDatabaseType() {
		return DB.HSQL.name();
	}

	public boolean createEntity(Dao dao, Entity<?> en) {
		StringBuilder sb = new StringBuilder("CREATE TABLE " + en.getTableName() + "(");
		// 创建字段
		for (MappingField mf : en.getMappingFields()) {
			sb.append('\n').append(mf.getColumnName());
			sb.append(' ').append(evalFieldType(mf));
			// 非主键的 @Name，应该加入唯一性约束
			if (mf.isName() && en.getPkType() != PkType.NAME) {
				sb.append(" UNIQUE NOT NULL");
			}
			// 普通字段
			else {
				if (mf.isUnsigned())
					sb.append(" UNSIGNED");
				if (mf.isAutoIncreasement()) // 自增与非空,不允许同时使用!
					sb.append(" GENERATED BY DEFAULT AS IDENTITY(START WITH 1)");
				else if (mf.isNotNull())
					sb.append(" NOT NULL");
				if (mf.hasDefaultValue())
					sb.append(" DEFAULT '").append(mf.getDefaultValue(null)).append('\'');
			}
			sb.append(',');
		}
		// 创建主键
		List<MappingField> pks = en.getPks();
		if (!pks.isEmpty()) {
			sb.append('\n');
			sb.append("PRIMARY KEY (");
			for (MappingField pk : pks) {
				sb.append(pk.getColumnName()).append(',');
			}
			sb.setCharAt(sb.length() - 1, ')');
			sb.append("\n ");
		}
		// 创建索引
		// TODO ...

		// 结束表字段设置
		sb.setCharAt(sb.length() - 1, ')');

		// 执行创建语句
		dao.execute(Sqls.create(sb.toString()));
		// 创建关联表
		createRelation(dao, en);

		return true;
	}

	@Override
	protected String evalFieldType(MappingField mf) {
		switch (mf.getColumnType()) {
		case INT:
			// 用户自定义了宽度
			if (mf.getWidth() > 0)
				return "NUMERIC(" + mf.getWidth() + ")";
			// 用数据库的默认宽度
			return "INT";

		case FLOAT:
			// 用户自定义了精度
			if (mf.getWidth() > 0 && mf.getPrecision() > 0) {
				return "NUMERIC(" + mf.getWidth() + "," + mf.getPrecision() + ")";
			}
			// 用默认精度
			if (mf.getTypeMirror().isDouble())
				return "NUMERIC(15,10)";
			return "FLOAT";
		}
		return super.evalFieldType(mf);
	}

	public void formatQuery(Pojo pojo) {
		Pager pager = pojo.getContext().getPager();
		if (pager == null)
			return;
		// See http://hsqldb.org/doc/guide/ch09.html#select-section
		pojo.append(Pojos.Items.wrapf(" LIMIT %d offset %d", pager.getPageSize(), pager.getOffset()));
	}

	protected String createResultSetMetaSql(Entity<?> en) {
		return "SELECT limit 1 1 * FROM " + en.getViewName();
	}
}
