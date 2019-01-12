package osbot.account.api.ipwhois;

public class WhoIsIp {

	private String ip, type, continent, continent_code, country, country_code, country_flag, country_phone,
			country_capital, region, city, latitude, longitude, asn, org, isp, timezone, timezone_name,
			timezone_dstOffset, timezone_gmtOffset, timezone_gmt, currency, currency_code, currency_symbol,
			currency_rates, currency_plural, country_neighbours;
	
	private int originalProxy;
	
	private long responseTime;

	private boolean success;

	public String getIp() {
		return ip;
	}

	public String getType() {
		return type;
	}

	public String getContinent() {
		return continent;
	}

	public String getContinent_code() {
		return continent_code;
	}

	public String getCountry() {
		return country;
	}

	public String getCountry_code() {
		return country_code;
	}

	public String getCountry_flag() {
		return country_flag;
	}

	public String getCountry_phone() {
		return country_phone;
	}

	public String getRegion() {
		return region;
	}

	public String getCity() {
		return city;
	}

	public String getLatitude() {
		return latitude;
	}

	public String getLongitude() {
		return longitude;
	}

	public String getAsn() {
		return asn;
	}

	public String getOrg() {
		return org;
	}

	public String getIsp() {
		return isp;
	}

	public String getTimezone() {
		return timezone;
	}

	public String getTimezone_name() {
		return timezone_name;
	}

	public String getTimezone_dstOffset() {
		return timezone_dstOffset;
	}

	public String getTimezone_gmt() {
		return timezone_gmt;
	}

	public String getCurrency() {
		return currency;
	}

	public String getCurrency_code() {
		return currency_code;
	}

	public String getCurrency_symbol() {
		return currency_symbol;
	}

	public String getCurrency_rates() {
		return currency_rates;
	}

	public String getCurrency_plural() {
		return currency_plural;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setContinent(String continent) {
		this.continent = continent;
	}

	public void setContinent_code(String continent_code) {
		this.continent_code = continent_code;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public void setCountry_code(String country_code) {
		this.country_code = country_code;
	}

	public void setCountry_flag(String country_flag) {
		this.country_flag = country_flag;
	}

	public void setCountry_phone(String country_phone) {
		this.country_phone = country_phone;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public void setAsn(String asn) {
		this.asn = asn;
	}

	public void setOrg(String org) {
		this.org = org;
	}

	public void setIsp(String isp) {
		this.isp = isp;
	}

	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

	public void setTimezone_name(String timezone_name) {
		this.timezone_name = timezone_name;
	}

	public void setTimezone_dstOffset(String timezone_dstOffset) {
		this.timezone_dstOffset = timezone_dstOffset;
	}

	public void setTimezone_gmt(String timezone_gmt) {
		this.timezone_gmt = timezone_gmt;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public void setCurrency_code(String currency_code) {
		this.currency_code = currency_code;
	}

	public void setCurrency_symbol(String currency_symbol) {
		this.currency_symbol = currency_symbol;
	}

	public void setCurrency_rates(String currency_rates) {
		this.currency_rates = currency_rates;
	}

	public void setCurrency_plural(String currency_plural) {
		this.currency_plural = currency_plural;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	/**
	 * @return the country_capital
	 */
	public String getCountry_capital() {
		return country_capital;
	}

	/**
	 * @param country_capital
	 *            the country_capital to set
	 */
	public void setCountry_capital(String country_capital) {
		this.country_capital = country_capital;
	}

	/**
	 * @return the timezone_gmtOffset
	 */
	public String getTimezone_gmtOffset() {
		return timezone_gmtOffset;
	}

	/**
	 * @param timezone_gmtOffset
	 *            the timezone_gmtOffset to set
	 */
	public void setTimezone_gmtOffset(String timezone_gmtOffset) {
		this.timezone_gmtOffset = timezone_gmtOffset;
	}

	/**
	 * @return the country_neighbours
	 */
	public String getCountry_neighbours() {
		return country_neighbours;
	}

	/**
	 * @param country_neighbours
	 *            the country_neighbours to set
	 */
	public void setCountry_neighbours(String country_neighbours) {
		this.country_neighbours = country_neighbours;
	}

	/**
	 * @return the responseTime
	 */
	public long getResponseTime() {
		return responseTime;
	}

	/**
	 * @param responseTime the responseTime to set
	 */
	public void setResponseTime(long responseTime) {
		this.responseTime = responseTime;
	}

	/**
	 * @return the originalProxy
	 */
	public int getOriginalProxy() {
		return originalProxy;
	}

	/**
	 * @param originalProxy the originalProxy to set
	 */
	public void setOriginalProxy(int originalProxy) {
		this.originalProxy = originalProxy;
	}
}
