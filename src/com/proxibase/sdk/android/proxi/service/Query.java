package com.proxibase.sdk.android.proxi.service;

import java.net.URLEncoder;

import com.proxibase.sdk.android.proxi.service.ProxibaseService.UrlEncodingType;

public class Query {

	public String			entityName;
	public String			filter;
	public Integer			topCount		= 0;
	public String			orderBy;
	public UrlEncodingType	urlEncodingType	= UrlEncodingType.All;

	public Query() {}

	public Query(String entityName) {
		this.entityName = entityName;
	}

	public Query filter(String filter) {
		this.filter = filter;
		return this;
	}

	public Query orderBy(String orderBy) {
		this.orderBy = orderBy;
		return this;
	}

	public Query top(Integer topCount) {
		this.topCount = topCount;
		return this;
	}

	public String queryString() {
		String query = this.entityName;
		Boolean atRoot = true;

		if (this.filter != null) {
			query += "?__find=" + URLEncoder.encode(this.filter);
			atRoot = false;
		}

		if (this.orderBy != null) {
			if (atRoot) {
				query += "?$orderby=" + URLEncoder.encode(this.orderBy);
			}
			else {
				query += "&$orderby=" + URLEncoder.encode(this.orderBy);
			}
			atRoot = false;
		}

		if (this.topCount != 0) {
			if (atRoot) {
				query += "?$top=" + URLEncoder.encode(this.topCount.toString());
			}
			else {
				query += "&$top=" + URLEncoder.encode(this.topCount.toString());
			}
			atRoot = false;
		}
		return query;
	}
}
