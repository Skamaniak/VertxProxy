package cz.jskrabal.proxy.support;

import io.vertx.core.http.HttpMethod;
import io.vertx.ext.unit.TestContext;

/**
 * Created by janskrabal on 08/07/16.
 */
public class TestServerRequest {
	private final TestContext context;
	private final HttpMethod httpMethod;
	private final String response;
	private final int responseCode;
	private final ResponseType responseType;

	public TestServerRequest(TestContext context, HttpMethod httpMethod, String response, int responseCode,
							 ResponseType responseType) {
		this.context = context;
		this.httpMethod = httpMethod;
		this.response = response;
		this.responseCode = responseCode;
		this.responseType = responseType;
	}

	public TestContext getContext() {
		return context;
	}

	public HttpMethod getHttpMethod() {
		return httpMethod;
	}

	public String getResponse() {
		return response;
	}

	public int getResponseCode() {
		return responseCode;
	}

	public ResponseType getResponseType() {
		return responseType;
	}

	public static Builder create(TestContext context, HttpMethod httpMethod) {
		return new Builder(context, httpMethod);
	}

	public static class Builder {
		private TestContext context;
		private HttpMethod httpMethod;
		private String response;
		private int responseCode = 200;
		private ResponseType responseType = ResponseType.RESPOND;

		public Builder(TestContext context, HttpMethod httpMethod) {
			this.context = context;
			this.httpMethod = httpMethod;
		}

		public Builder withResponse(String response) {
			this.response = response;
			return this;
		}

		public Builder withResponseCode(int responseCode) {
			this.responseCode = responseCode;
			return this;
		}

		public Builder withResponseType(ResponseType responseType) {
			this.responseType = responseType;
			return this;
		}

		public TestServerRequest build() {
			return new TestServerRequest(context, httpMethod, response, responseCode, responseType);
		}
	}
}
