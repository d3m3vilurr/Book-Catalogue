/*
 * @copyright 2011 Sunguk Lee
 * @license GNU General Public License
 * 
 * This file is part of Book Catalogue.
 *
 * Book Catalogue is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Book Catalogue is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Book Catalogue.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.eleybourn.bookcatalogue;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/* 
 * An HTML handler for the Aladin Books return 
 * 
 * An example response looks like;
 * 
 * <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
 * <html xmlns="http://www.w3.org/1999/xhtml" lang="ko">
 *   <head>
 *     <title>알라딘모바일</title>
 * ...
 *   </head>
 *   <body>
 * ...
 *     <div id="pn_searchResult">
 * ...
 *       <div class="pb_list_box">
 *         <div onclick="location.href='http://m.aladin.co.kr/m/mproduct.aspx?ISBN=059035342X'" style="cursor:pointer;">
 *         <div class="pb_list_imgbox">
 *           <span><img src="http://image.aladin.co.kr/product/20/85/coversum/059035342x_2.jpg" width="75" /></span>
 *         </div>
 *         <div class="pb_list_info">
 *           <ul>
 *             <li><span class="pb_title">Harry Potter and the Sorcerer'...</span></li>
 *             <li><a href="/m/msearch.aspx?authorsearch=조앤+k.+롤링@68403&branchtype=1">조앤 k. 롤링</a>  </li>
 *             <li><span class="nm_f_p">8,580</span>원 (<span class="nm_f_p">40%</span>↓<img src="http://image.aladdin.co.kr/img/icon/m.gif" style="margin-top:-3px;" /><span class="nm_f_m">430</span>)</li>
 *             <li>1999-09-01</li>
 *             <li><img src="http://image.aladdin.co.kr/img/common/star_s9.gif" align="absmiddle" /> <img src="http://image.aladdin.co.kr/img/m/2010/nm_salespoint.jpg" /> : 41,794 </li>
 *           </ul>
 *         </div>
 *       </div>
 * ...
 *   </body>
 * </html>
 */

public class SearchAladinBooksHandler {
	private String id = "";
	private int count = 0;
	private String published = "";
	private Document mDocument = null;
	
	public static String SELECTOR = ".pb_list_box";
		
	public SearchAladinBooksHandler(String url) {
		try {
			mDocument = Jsoup.connect(url).get();
			parse();
		} catch (IOException e) {
		}
	}
	
	private void parse() {
		Elements results = mDocument.select(SELECTOR);
		count = results.size();
		if (results.size() > 0) {
			Element element = results.get(0);
			parseId(element);
			parseDatePublished(element);
		}
	}

	private void parseId(Element element) {
		String onclick = element.select("div[onclick]").get(0).attr("onclick");
		id = onclick.replaceAll("location\\.href=\'(.+)\'", "$1");
	}

	private void parseDatePublished(Element element) {
		published = element.select(".pb_list_info li").get(3).text();
	}


	/**
	 * Return the id of the first book found
	 * 
	 * @return The book id (to be passed to the entry handler)
	 */
	public String getId(){
		return id;
	}
	
	/**
	 * How many books were found?
	 * 
	 * @return The number of books found
	 */
	public int getCount(){
		return count;
	}

	public String getDatePublished() {
		return published;
	}
}