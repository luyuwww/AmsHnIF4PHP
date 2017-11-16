package cn.lams.pojo;

import java.util.ArrayList;
import java.util.List;

public class SRoleExample {
    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table S_ROLE
     *
     * @mbggenerated
     */
    protected String orderByClause;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table S_ROLE
     *
     * @mbggenerated
     */
    protected boolean distinct;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table S_ROLE
     *
     * @mbggenerated
     */
    protected List<Criteria> oredCriteria;

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table S_ROLE
     *
     * @mbggenerated
     */
    public SRoleExample() {
        oredCriteria = new ArrayList<Criteria>();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table S_ROLE
     *
     * @mbggenerated
     */
    public void setOrderByClause(String orderByClause) {
        this.orderByClause = orderByClause;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table S_ROLE
     *
     * @mbggenerated
     */
    public String getOrderByClause() {
        return orderByClause;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table S_ROLE
     *
     * @mbggenerated
     */
    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table S_ROLE
     *
     * @mbggenerated
     */
    public boolean isDistinct() {
        return distinct;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table S_ROLE
     *
     * @mbggenerated
     */
    public List<Criteria> getOredCriteria() {
        return oredCriteria;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table S_ROLE
     *
     * @mbggenerated
     */
    public void or(Criteria criteria) {
        oredCriteria.add(criteria);
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table S_ROLE
     *
     * @mbggenerated
     */
    public Criteria or() {
        Criteria criteria = createCriteriaInternal();
        oredCriteria.add(criteria);
        return criteria;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table S_ROLE
     *
     * @mbggenerated
     */
    public Criteria createCriteria() {
        Criteria criteria = createCriteriaInternal();
        if (oredCriteria.size() == 0) {
            oredCriteria.add(criteria);
        }
        return criteria;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table S_ROLE
     *
     * @mbggenerated
     */
    protected Criteria createCriteriaInternal() {
        Criteria criteria = new Criteria();
        return criteria;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table S_ROLE
     *
     * @mbggenerated
     */
    public void clear() {
        oredCriteria.clear();
        orderByClause = null;
        distinct = false;
    }

    /**
     * This class was generated by MyBatis Generator.
     * This class corresponds to the database table S_ROLE
     *
     * @mbggenerated
     */
    protected abstract static class GeneratedCriteria {
        protected List<Criterion> criteria;

        protected GeneratedCriteria() {
            super();
            criteria = new ArrayList<Criterion>();
        }

        public boolean isValid() {
            return criteria.size() > 0;
        }

        public List<Criterion> getAllCriteria() {
            return criteria;
        }

        public List<Criterion> getCriteria() {
            return criteria;
        }

        protected void addCriterion(String condition) {
            if (condition == null) {
                throw new RuntimeException("Value for condition cannot be null");
            }
            criteria.add(new Criterion(condition));
        }

        protected void addCriterion(String condition, Object value, String property) {
            if (value == null) {
                throw new RuntimeException("Value for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value));
        }

        protected void addCriterion(String condition, Object value1, Object value2, String property) {
            if (value1 == null || value2 == null) {
                throw new RuntimeException("Between values for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value1, value2));
        }

        public Criteria andDidIsNull() {
            addCriterion("DID is null");
            return (Criteria) this;
        }

        public Criteria andDidIsNotNull() {
            addCriterion("DID is not null");
            return (Criteria) this;
        }

        public Criteria andDidEqualTo(Integer value) {
            addCriterion("DID =", value, "did");
            return (Criteria) this;
        }

        public Criteria andDidNotEqualTo(Integer value) {
            addCriterion("DID <>", value, "did");
            return (Criteria) this;
        }

        public Criteria andDidGreaterThan(Integer value) {
            addCriterion("DID >", value, "did");
            return (Criteria) this;
        }

        public Criteria andDidGreaterThanOrEqualTo(Integer value) {
            addCriterion("DID >=", value, "did");
            return (Criteria) this;
        }

        public Criteria andDidLessThan(Integer value) {
            addCriterion("DID <", value, "did");
            return (Criteria) this;
        }

        public Criteria andDidLessThanOrEqualTo(Integer value) {
            addCriterion("DID <=", value, "did");
            return (Criteria) this;
        }

        public Criteria andDidIn(List<Integer> values) {
            addCriterion("DID in", values, "did");
            return (Criteria) this;
        }

        public Criteria andDidNotIn(List<Integer> values) {
            addCriterion("DID not in", values, "did");
            return (Criteria) this;
        }

        public Criteria andDidBetween(Integer value1, Integer value2) {
            addCriterion("DID between", value1, value2, "did");
            return (Criteria) this;
        }

        public Criteria andDidNotBetween(Integer value1, Integer value2) {
            addCriterion("DID not between", value1, value2, "did");
            return (Criteria) this;
        }

        public Criteria andJsmcIsNull() {
            addCriterion("JSMC is null");
            return (Criteria) this;
        }

        public Criteria andJsmcIsNotNull() {
            addCriterion("JSMC is not null");
            return (Criteria) this;
        }

        public Criteria andJsmcEqualTo(String value) {
            addCriterion("JSMC =", value, "jsmc");
            return (Criteria) this;
        }

        public Criteria andJsmcNotEqualTo(String value) {
            addCriterion("JSMC <>", value, "jsmc");
            return (Criteria) this;
        }

        public Criteria andJsmcGreaterThan(String value) {
            addCriterion("JSMC >", value, "jsmc");
            return (Criteria) this;
        }

        public Criteria andJsmcGreaterThanOrEqualTo(String value) {
            addCriterion("JSMC >=", value, "jsmc");
            return (Criteria) this;
        }

        public Criteria andJsmcLessThan(String value) {
            addCriterion("JSMC <", value, "jsmc");
            return (Criteria) this;
        }

        public Criteria andJsmcLessThanOrEqualTo(String value) {
            addCriterion("JSMC <=", value, "jsmc");
            return (Criteria) this;
        }

        public Criteria andJsmcLike(String value) {
            addCriterion("JSMC like", value, "jsmc");
            return (Criteria) this;
        }

        public Criteria andJsmcNotLike(String value) {
            addCriterion("JSMC not like", value, "jsmc");
            return (Criteria) this;
        }

        public Criteria andJsmcIn(List<String> values) {
            addCriterion("JSMC in", values, "jsmc");
            return (Criteria) this;
        }

        public Criteria andJsmcNotIn(List<String> values) {
            addCriterion("JSMC not in", values, "jsmc");
            return (Criteria) this;
        }

        public Criteria andJsmcBetween(String value1, String value2) {
            addCriterion("JSMC between", value1, value2, "jsmc");
            return (Criteria) this;
        }

        public Criteria andJsmcNotBetween(String value1, String value2) {
            addCriterion("JSMC not between", value1, value2, "jsmc");
            return (Criteria) this;
        }

        public Criteria andIsxtmrIsNull() {
            addCriterion("ISXTMR is null");
            return (Criteria) this;
        }

        public Criteria andIsxtmrIsNotNull() {
            addCriterion("ISXTMR is not null");
            return (Criteria) this;
        }

        public Criteria andIsxtmrEqualTo(Integer value) {
            addCriterion("ISXTMR =", value, "isxtmr");
            return (Criteria) this;
        }

        public Criteria andIsxtmrNotEqualTo(Integer value) {
            addCriterion("ISXTMR <>", value, "isxtmr");
            return (Criteria) this;
        }

        public Criteria andIsxtmrGreaterThan(Integer value) {
            addCriterion("ISXTMR >", value, "isxtmr");
            return (Criteria) this;
        }

        public Criteria andIsxtmrGreaterThanOrEqualTo(Integer value) {
            addCriterion("ISXTMR >=", value, "isxtmr");
            return (Criteria) this;
        }

        public Criteria andIsxtmrLessThan(Integer value) {
            addCriterion("ISXTMR <", value, "isxtmr");
            return (Criteria) this;
        }

        public Criteria andIsxtmrLessThanOrEqualTo(Integer value) {
            addCriterion("ISXTMR <=", value, "isxtmr");
            return (Criteria) this;
        }

        public Criteria andIsxtmrIn(List<Integer> values) {
            addCriterion("ISXTMR in", values, "isxtmr");
            return (Criteria) this;
        }

        public Criteria andIsxtmrNotIn(List<Integer> values) {
            addCriterion("ISXTMR not in", values, "isxtmr");
            return (Criteria) this;
        }

        public Criteria andIsxtmrBetween(Integer value1, Integer value2) {
            addCriterion("ISXTMR between", value1, value2, "isxtmr");
            return (Criteria) this;
        }

        public Criteria andIsxtmrNotBetween(Integer value1, Integer value2) {
            addCriterion("ISXTMR not between", value1, value2, "isxtmr");
            return (Criteria) this;
        }

        public Criteria andBzIsNull() {
            addCriterion("BZ is null");
            return (Criteria) this;
        }

        public Criteria andBzIsNotNull() {
            addCriterion("BZ is not null");
            return (Criteria) this;
        }

        public Criteria andBzEqualTo(String value) {
            addCriterion("BZ =", value, "bz");
            return (Criteria) this;
        }

        public Criteria andBzNotEqualTo(String value) {
            addCriterion("BZ <>", value, "bz");
            return (Criteria) this;
        }

        public Criteria andBzGreaterThan(String value) {
            addCriterion("BZ >", value, "bz");
            return (Criteria) this;
        }

        public Criteria andBzGreaterThanOrEqualTo(String value) {
            addCriterion("BZ >=", value, "bz");
            return (Criteria) this;
        }

        public Criteria andBzLessThan(String value) {
            addCriterion("BZ <", value, "bz");
            return (Criteria) this;
        }

        public Criteria andBzLessThanOrEqualTo(String value) {
            addCriterion("BZ <=", value, "bz");
            return (Criteria) this;
        }

        public Criteria andBzLike(String value) {
            addCriterion("BZ like", value, "bz");
            return (Criteria) this;
        }

        public Criteria andBzNotLike(String value) {
            addCriterion("BZ not like", value, "bz");
            return (Criteria) this;
        }

        public Criteria andBzIn(List<String> values) {
            addCriterion("BZ in", values, "bz");
            return (Criteria) this;
        }

        public Criteria andBzNotIn(List<String> values) {
            addCriterion("BZ not in", values, "bz");
            return (Criteria) this;
        }

        public Criteria andBzBetween(String value1, String value2) {
            addCriterion("BZ between", value1, value2, "bz");
            return (Criteria) this;
        }

        public Criteria andBzNotBetween(String value1, String value2) {
            addCriterion("BZ not between", value1, value2, "bz");
            return (Criteria) this;
        }
    }

    /**
     * This class was generated by MyBatis Generator.
     * This class corresponds to the database table S_ROLE
     *
     * @mbggenerated do_not_delete_during_merge
     */
    public static class Criteria extends GeneratedCriteria {

        protected Criteria() {
            super();
        }
    }

    /**
     * This class was generated by MyBatis Generator.
     * This class corresponds to the database table S_ROLE
     *
     * @mbggenerated
     */
    public static class Criterion {
        private String condition;

        private Object value;

        private Object secondValue;

        private boolean noValue;

        private boolean singleValue;

        private boolean betweenValue;

        private boolean listValue;

        private String typeHandler;

        public String getCondition() {
            return condition;
        }

        public Object getValue() {
            return value;
        }

        public Object getSecondValue() {
            return secondValue;
        }

        public boolean isNoValue() {
            return noValue;
        }

        public boolean isSingleValue() {
            return singleValue;
        }

        public boolean isBetweenValue() {
            return betweenValue;
        }

        public boolean isListValue() {
            return listValue;
        }

        public String getTypeHandler() {
            return typeHandler;
        }

        protected Criterion(String condition) {
            super();
            this.condition = condition;
            this.typeHandler = null;
            this.noValue = true;
        }

        protected Criterion(String condition, Object value, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.typeHandler = typeHandler;
            if (value instanceof List<?>) {
                this.listValue = true;
            } else {
                this.singleValue = true;
            }
        }

        protected Criterion(String condition, Object value) {
            this(condition, value, null);
        }

        protected Criterion(String condition, Object value, Object secondValue, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.secondValue = secondValue;
            this.typeHandler = typeHandler;
            this.betweenValue = true;
        }

        protected Criterion(String condition, Object value, Object secondValue) {
            this(condition, value, secondValue, null);
        }
    }
}