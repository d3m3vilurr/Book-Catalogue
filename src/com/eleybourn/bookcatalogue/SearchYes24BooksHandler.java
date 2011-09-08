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
import org.jsoup.select.Elements;

/* 
 * An HTML handler for the Yes24 Books return 
 * 
 * An example response looks like;
 * 
 * ...
 * <!-- 상품 이미지 및 버튼 끝 -->
 * </td>
 * <td valign="top">
 *   [외서]
 *   <a href="/24/goods/294819?scode=032&amp;OzSrank=1"><b>Harry Potter and the Sorcerer&#39;s Stone</b></a>  : Book 1
 *   (Paperback, 미국판)
 *   <br>
 *   <div style="margin-top: 5px;">
 *     <span class="txtAp">
 *       <a href="http://www.yes24.com/SearchCorner/Result?scode=032&OzSrank=1&domain=ALL&author_yn=Y&query=J.+K.+Rowling">J. K. Rowling</a>,
 *       <a href="http://www.yes24.com/SearchCorner/Result?scode=032&OzSrank=1&domain=ALL&author_yn=Y&query=Mary+GrandPre">Mary GrandPre</a> |
 *       <a href="http://www.yes24.com/SearchCorner/Result?scode=032&OzSrank=1&domain=ALL&company_yn=Y&query=Arthur A. Levine Books">Arthur A. Levine Books</a> |
 *       1999년 09월
 *     </span>
 *     <div style="margin-top: 5px;">
 *       14,300원 →<span class="priceB">6,500원</span>(<span class="price">55%</span> 할인) |
 *       YES포인트 <span class="price">130원</span>(<span class="price">2%</span> 지급)
 *       <br />
 *       <div style="margin-top: 5px;">
 *         도착 예상일 : 지금 주문하면 <strong> 오늘 </strong> 받을 수 있습니다.
 *       </div>
 * ...
 */
public class SearchYes24BooksHandler {
	public String id = "";
	public int count = 0;
	private Document mDocument = null;
	
	public static String SELECTOR = "#category_layout tbody tr td a";
		
	public SearchYes24BooksHandler(String url) {
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
			id = results.get(0).attr("href");
		}
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
}
