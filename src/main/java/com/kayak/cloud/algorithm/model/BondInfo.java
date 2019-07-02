package com.kayak.cloud.algorithm.model;

/**
 * 债券基本信息
 * @author pc_xjf
 */
public class BondInfo extends FixedIncome{

	/**债券识别号=市场代码+债券代码*/
	private String bond_id;
	/**市场代码 市场代码:1——上交所 2——深交所 3——银行间*/
	private String market_code;
	/**债券全称*/
	private String full_name;
	/**债券属性*/
	private Integer bond_prop=1;
	/**上市日期*/
	private String publish_date;
	/**基础利率*/
	private String base_rate;
	/**利差*/
	private String bond_spread;
	/**基础利率来源*/
	private Integer baserate_type;
	/**计息方式 1-单利 2-复利*/
	private Integer calint_type=1;
	/**息票品种:1——附加息 2——零息 3——贴现*/
	private Integer interest_type=1;
	/**附息利率品种:1——浮动利率 2——固定利率 3——累进利率*/
	private Integer interest_sort=2;
	/**付息频率:1—— 按月付息 2—— 按季付息 3——半年付息 4—— 按年付息 5——到期付息*/
	private Integer pay_freq=4;
	/**发行主体*/
	private String publisher;
	/**发行价格*/
	private String issue_price;
	/**发行收益率*/
	private String issue_rate;
	/**发行量*/
	private String issue_volume="0";
	/**托管场所:1——上交所 2——深交所 3——中债登 4——上清所*/
	private Integer depot=3;
	/**担保人*/
	private String guarantor;
	/**担保方式*/
	private Integer guar_type;
	/**是否含权:0——不含权 1——含权*/
	private Integer is_exercise=0;
	/**行权方式:1——投资人可回售券 2——发行人可赎回券*/
	private Integer exercise_type;
	/**后期票面*/
	private String end_rate;
	/**还本方式:1——到期还本 2——提前还本*/
	private Integer repayment_type=1;
	/**计息模式：1为分段计息，2为逐日计息*/
	private Integer interest_mode;
	/**提前还本方式：1.本金打折，面额不变，2.本金打折，面额打折*/
	private Integer principal_mode;
	/**自定义付息日*/
	private String data_list;
	/**是否为abs 1：是 0：否*/
	private String is_abs="0";
	/**贴现债 银行间实际兑付日 */
	private String pay_date;

	public String getBond_id() {
		return bond_id;
	}
	public void setBond_id(String bondId) {
		bond_id = bondId;
	}
	public String getMarket_code() {
		return market_code;
	}
	public void setMarket_code(String marketCode) {
		market_code = marketCode;
	}
	public String getFull_name() {
		return full_name;
	}
	public void setFull_name(String fullName) {
		full_name = fullName;
	}
	public Integer getBond_prop() {
		return bond_prop;
	}
	public void setBond_prop(Integer bondProp) {
		bond_prop = bondProp;
	}
	public String getPublish_date() {
		return publish_date;
	}
	public void setPublish_date(String publishDate) {
		publish_date = publishDate;
	}
	public String getBase_rate() {
		return base_rate;
	}
	public void setBase_rate(String baseRate) {
		base_rate = baseRate;
	}
	public String getBond_spread() {
		return bond_spread;
	}
	public void setBond_spread(String bondSpread) {
		bond_spread = bondSpread;
	}
	public Integer getBaserate_type() {
		return baserate_type;
	}
	public void setBaserate_type(Integer baserateType) {
		baserate_type = baserateType;
	}
	public Integer getCalint_type() {
		return calint_type;
	}
	public void setCalint_type(Integer calintType) {
		calint_type = calintType;
	}
	public Integer getInterest_type() {
		return interest_type;
	}
	public void setInterest_type(Integer interestType) {
		interest_type = interestType;
	}
	public Integer getInterest_sort() {
		return interest_sort;
	}
	public void setInterest_sort(Integer interestSort) {
		interest_sort = interestSort;
	}
	public Integer getPay_freq() {
		return pay_freq;
	}
	public void setPay_freq(Integer payFreq) {
		pay_freq = payFreq;
	}
	public String getPublisher() {
		return publisher;
	}
	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}
	public String getIssue_price() {
		return issue_price;
	}
	public void setIssue_price(String issuePrice) {
		issue_price = issuePrice;
	}
	public String getIssue_rate() {
		return issue_rate;
	}
	public void setIssue_rate(String issueRate) {
		issue_rate = issueRate;
	}
	public String getIssue_volume() {
		return issue_volume;
	}
	public void setIssue_volume(String issueVolume) {
		issue_volume = issueVolume;
	}
	public Integer getDepot() {
		return depot;
	}
	public void setDepot(Integer depot) {
		this.depot = depot;
	}
	public String getGuarantor() {
		return guarantor;
	}
	public void setGuarantor(String guarantor) {
		this.guarantor = guarantor;
	}
	public Integer getGuar_type() {
		return guar_type;
	}
	public void setGuar_type(Integer guarType) {
		guar_type = guarType;
	}
	public Integer getIs_exercise() {
		return is_exercise;
	}
	public void setIs_exercise(Integer isExercise) {
		is_exercise = isExercise;
	}
	public Integer getExercise_type() {
		return exercise_type;
	}
	public void setExercise_type(Integer exerciseType) {
		exercise_type = exerciseType;
	}
	public String getEnd_rate() {
		return end_rate;
	}
	public void setEnd_rate(String endRate) {
		end_rate = endRate;
	}
	public Integer getRepayment_type() {
		if(repayment_type == null){
			repayment_type=1;
		}
		return repayment_type;
	}
	public void setRepayment_type(Integer repaymentType) {
		repayment_type = repaymentType;
	}

	public Integer getInterest_mode() {
		if(interest_mode == null ){
			interest_mode = 1;
		}
		return interest_mode;
	}
	public void setInterest_mode(Integer interest_mode) {
		this.interest_mode = interest_mode;
	}
	public Integer getPrincipal_mode() {
		return principal_mode;
	}
	public void setPrincipal_mode(Integer principal_mode) {
		this.principal_mode = principal_mode;
	}
	public String getData_list() {
		return data_list;
	}
	public void setData_list(String data_list) {
		this.data_list = data_list;
	}

	public String getIs_abs() {
		if(bond_prop == 8 ){
			is_abs = "1";
		}
		return is_abs;
	}
	public void setIs_abs(String is_abs) {
		this.is_abs = is_abs;
	}
	public String getPay_date() {
		return pay_date;
	}
	public void setPay_date(String pay_date) {
		this.pay_date = pay_date;
	}
	@Override
	public String toString() {
		return "BondInfo [bond_id=" + bond_id + ", market_code=" + market_code + ", full_name=" + full_name
				+ ", bond_prop=" + bond_prop + ", publish_date=" + publish_date + ", base_rate=" + base_rate
				+ ", bond_spread=" + bond_spread + ", baserate_type=" + baserate_type + ", calint_type=" + calint_type
				+ ", interest_type=" + interest_type + ", interest_sort=" + interest_sort + ", pay_freq=" + pay_freq
				+ ", publisher=" + publisher + ", issue_price=" + issue_price + ", issue_rate=" + issue_rate
				+ ", issue_volume=" + issue_volume + ", depot=" + depot + ", guarantor=" + guarantor + ", guar_type="
				+ guar_type + ", is_exercise=" + is_exercise + ", exercise_type=" + exercise_type + ", end_rate="
				+ end_rate + ", repayment_type=" + repayment_type + ", interest_mode=" + interest_mode
				+ ", principal_mode=" + principal_mode + ", data_list=" + data_list + ", is_abs=" + is_abs
				+ ", pay_date=" + pay_date + "]";
	}

	public BondInfo(String bond_id, String market_code, String full_name, Integer bond_prop, String publish_date,
					String base_rate, String bond_spread, Integer baserate_type, Integer calint_type, Integer interest_type,
					Integer interest_sort, Integer pay_freq, String publisher, String issue_price, String issue_rate,
					String issue_volume, Integer depot, String guarantor, Integer guar_type, Integer is_exercise,
					Integer exercise_type, String end_rate, Integer repayment_type, Integer interest_mode,
					Integer principal_mode, String data_list, String is_abs, String pay_date) {
		super();
		this.bond_id = bond_id;
		this.market_code = market_code;
		this.full_name = full_name;
		this.bond_prop = bond_prop;
		this.publish_date = publish_date;
		this.base_rate = base_rate;
		this.bond_spread = bond_spread;
		this.baserate_type = baserate_type;
		this.calint_type = calint_type;
		this.interest_type = interest_type;
		this.interest_sort = interest_sort;
		this.pay_freq = pay_freq;
		this.publisher = publisher;
		this.issue_price = issue_price;
		this.issue_rate = issue_rate;
		this.issue_volume = issue_volume;
		this.depot = depot;
		this.guarantor = guarantor;
		this.guar_type = guar_type;
		this.is_exercise = is_exercise;
		this.exercise_type = exercise_type;
		this.end_rate = end_rate;
		this.repayment_type = repayment_type;
		this.interest_mode = interest_mode;
		this.principal_mode = principal_mode;
		this.data_list = data_list;
		this.is_abs = is_abs;
		this.pay_date = pay_date;
	}

	public BondInfo() {
		super();	}
}
