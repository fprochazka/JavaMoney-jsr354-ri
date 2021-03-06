/*
  Copyright (c) 2012, 2015, Credit Suisse (Anatole Tresch), Werner Keil and others by the @author tag.

  Licensed under the Apache License, Version 2.0 (the "License"); you may not
  use this file except in compliance with the License. You may obtain a copy of
  the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  License for the specific language governing permissions and limitations under
  the License.
 */
package org.javamoney.moneta.convert.imf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.time.YearMonth;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.javamoney.moneta.convert.imf.IMFRemoteSearchCallable.IMFRemoteSearchResult;


class IMFRemoteSearchCallable implements Callable<IMFRemoteSearchResult>{

	private static final Logger LOG = Logger.getLogger(IMFRemoteSearch.class.getName());

	private final IMFHistoricalType type;

	private final YearMonth yearMonth;
	private final String userAgent;

	IMFRemoteSearchCallable(IMFHistoricalType type, YearMonth yearMonth, String userAgent) {
		this.type = Objects.requireNonNull(type);
		this.yearMonth = Objects.requireNonNull(yearMonth);
		this.userAgent = Objects.requireNonNull(userAgent);
	}


	@Override
	public IMFRemoteSearchResult call() throws Exception {

		URLConnection connection = getConnection();
		if(Objects.isNull(connection)) {
			return null;
		}
		connection.addRequestProperty("User-Agent", userAgent);
        try (InputStream inputStream = connection.getInputStream(); ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            byte[] data = new byte[4096];
            int read = inputStream.read(data);
            while (read > 0) {
                stream.write(data, 0, read);
                read = inputStream.read(data);
            }
            return new IMFRemoteSearchResult(type, new ByteArrayInputStream(stream.toByteArray()));
        } catch (Exception e) {
            LOG.log(Level.INFO, "Failed to load resource from url " + type.getUrl(yearMonth), e);
        }
		return null;
	}


	private URLConnection getConnection() {
		try {
			return new URL(type.getUrl(yearMonth)).openConnection();
		} catch (Exception e) {
			LOG.log(Level.INFO, "Failed to load resource from url "
					+ type.getUrl(yearMonth), e);
		}
		return null;
	}

	@Override
	public String toString() {
        String sb = IMFRemoteSearchCallable.class.getName() + '{' +
                " type: " + type + ", yearMonth: " + yearMonth + '}';
        return sb;
	}

	class IMFRemoteSearchResult {

		private final IMFHistoricalType type;

		private final InputStream stream;

		IMFRemoteSearchResult(IMFHistoricalType type, InputStream stream) {
			this.type = type;
			this.stream = stream;
		}

		public IMFHistoricalType getType() {
			return type;
		}

		public InputStream getStream() {
			return stream;
		}

		@Override
		public String toString() {
            String sb = IMFRemoteSearchResult.class.getName() + '{' +
                    " type: " + type + ", stream: " + stream + '}';
            return sb;
		}
	}

}
