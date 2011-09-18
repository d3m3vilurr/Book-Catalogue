package com.eleybourn.bookcatalogue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.os.Bundle;

/* 
 * An HTML handler for the Aladin Books entry return 
 * 
 * <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
 * <html xmlns="http://www.w3.org/1999/xhtml" >
 *   <head>
 *     <title>알라딘모바일</title>
 * ...
 *   <head>
 *   <body>
 * ...
 *     <div id="m_product_head1_ph_mobile">
 * ...    
 *       <!-- 도서 서지 정보 -->
 *       <div class="mp_book_box">
 *         <div class="mp_book_imgbox">
 *           <div style="position:relative;float:left;cursor:pointer;" onclick="fn_go_letslook('059035342X')">
 *             <span class="mp_book_img"><img src="http://image.aladin.co.kr/product/20/85/cover150/059035342x_2.jpg" width="100" /></span>
 *             <span style="position:absolute;right:0; bottom:0;margin-right:15px;padding:1px;">
 *               <img src="http://image.aladin.co.kr/img/m/2010/icon_large.png" />
 *             </span>
 *           </div>
 * ...
 *         </div>
 *         <dl class="mp_book_info">
 *           <dt> Harry Potter Series, Book 1 </dt>
 *           <dt>
 *             <a href="http://m.aladin.co.kr/m/mproduct.aspx?ISBN=059035342X"
 *                class="nm_book_title">Harry Potter and the Sorcerer's Stone : Book 1 (Paperback, 미국판)</a>
 *           </dt>
 *           <dt class="nm_book_title_s">
 *             <a href="/m/msearch.aspx?authorsearch=조앤+k.+롤링@68403&branchtype=1">조앤 k. 롤링</a> (지은이) |
 *             <a href='/m/msearch.aspx?publishersearch=scholastic@8761&branchtype=7' >scholastic</a> |
 *             1999년 9월
 *           </dt>
 *         </dl>
 * ...
 *       </div>
 *       <input type="hidden" id="hd_ISBN" value="059035342X" />
 *       <!--// 도서 서지 정보 -->
 * ...
 *       <!-- 판매가 -->
 *       <div class="book_cb2">
 *         <div class="book_conts_left1">
 *           <ul>
 *             <span id="m_product_head1_txt_PriceInfo">
 *               <li>판매가 : <span class="nm_f_p">8,580</span>원 (<span class="jm_f_all">40%</span> off) <img src="http://image.aladin.co.kr/img/m/2010/btn_ue.jpg" onclick="fn_go_productSub('059035342X', 'Price')" style="cursor:pointer" /></li>
 *             </span>
 *             <span id="m_product_head1_txt_MileageInfo"><li>마일리지 : <span class="jm_f_all">430</span>원</li></span>
 *             <span id="m_product_head1_txt_Satang"></span>
 *             <span id="m_product_head1_txt_DeliveryInfo"><li>배송료 : 유료 (단, 도서 1만원 이상 또는 신간 1권 포함시 무료)</li></span>
 *             <span id="m_product_head1_txt_Rank"><li>평점 : <img src="http://image.aladdin.co.kr/img/common/star_s9.gif" border="0" /></li></span>
 *             <span id="m_product_head1_txt_PackingInfo">
 *               <li>반양장본 | 312쪽 | 197*133mm | 언어 : English</li>
 *               <li>ISBN(13) : 9780590353427</li>
 *             </span>
 *             <span id="m_product_head1_ItemWarningMessage"><li><span class="nm_notice">단어장을 다운로드 하시려면 <a href="/events/wevent_foreign_m.aspx?pn=081121_pdf">여기</a>를 누르세요.</span></li></span>            
 *           </ul>
 *         </div>
 *       </div>
 *       <!-- //판매가 -->
 * ...
 *     </div>
 *     <!-- 책소개 -->
 *     <div class="book_cb2" onclick="getMoreContents('Description', 39566, 7)">
 *       <h2><span id="m_product_introduce1_txtDescTitle">책 소개</span></h2> 
 *       <ul class="book_conts">
 *         <li>
 *           <div id="Description">
 *             해리 포터는 위압적인 버논 숙부와 냉담한 이모 페투니아, 욕심 많고 버릇없는 사촌 더즐리 밑에서 갖은 구박을 견디며 계단 밑 벽장에서 생활한다.<BR>
 *             <BR>
 *             그러던 중 ..
 *             <li>
 *               <a href="javascript:getMoreContents('Description', 39566, 7)" >
 *                 <img src="http://image.aladdin.co.kr/img/m/2010/btn_more_02.jpg" style="float:right"/ >
 *               </a>
 *             </li>
 *           </div>
 *         </li>
 *       </ul>
 *     </div>
 * ...
 *     <!-- 저자소개 시작 -->
 *     <div class="book_cb1" onclick="fn_go_productSub('059035342X', 'Author')">
 *       <div class="book_conts_left">
 *         <h2><span id="m_product_authorInfo1_txtTitle">저자 및 역자 소개</span></h2> 
 *         <ul class="book_conts">
 *           <li><span id="m_product_authorInfo1_txtAuthorInfo">조앤 K. 롤링 (지은이)</span></li> 
 *         </ul>
 *       </div>
 *     </div>
 * ...
 *     <!-- 분류 시작 -->
 *     <div class="book_cb2">
 *       <div class="book_conts_left">
 *         <h2>관련 주제 분류</h2> 
 *         <div class="book_conts2">
 *           <ul>
 *             <li><a href='/m/mbrowse.aspx?CID=25548' class='nm_f_d'><b>어린이</b></a></li>
 *             <li><a href='/m/mbrowse.aspx?CID=25547' class='nm_f_d'>문학</a>&nbsp;>&nbsp;<a href='/m/mbrowse.aspx?CID=25601' class='nm_f_d'>소설</a>&nbsp;>&nbsp;<a href='/m/mbrowse.aspx?CID=25766&BrowseTarget=List' class='nm_f_d'><b>일반소설</b></a></li>
 *             <li><a href='/m/mbrowse.aspx?CID=25547' class='nm_f_d'>문학</a>&nbsp;>&nbsp;<a href='/m/mbrowse.aspx?CID=25601' class='nm_f_d'>소설</a>&nbsp;>&nbsp;<a href='/m/mbrowse.aspx?CID=25934' class='nm_f_d'><b>SF/판타지/스릴러/추리</b></a></li>
 *             <li><a href='/m/mbrowse.aspx?CID=25548' class='nm_f_d'>어린이</a>&nbsp;>&nbsp;<a href='/m/mbrowse.aspx?CID=50253' class='nm_f_d'>AR Reading Level</a>&nbsp;>&nbsp;<a href='/m/mbrowse.aspx?CID=50263&BrowseTarget=List' class='nm_f_d'><b>5.1 ~ 5.5</b></a></li>
 *           </ul>
 *         </div>	
 *       </div>
 *     </div>
 * ...
 *   </body>
 * </html>
 */

public class SearchAladinBooksEntryHandler {
	private Document mDocument = null;
	private Bundle mValues;
	private boolean mFetchThumbnail;
	
	public SearchAladinBooksEntryHandler(String url, Bundle values, boolean fetchThumbnail) {
		mValues = values;
		mFetchThumbnail = fetchThumbnail;
		try {
			mDocument = Jsoup.connect(url).get();
			parse();
		} catch (IOException e) {
		}
	}
	
	private void addIfNotPresent(String key, String value) {
		if (!mValues.containsKey(key) || mValues.getString(key).length() == 0) {
			mValues.putString(key, value);
		}		
	}
	
	private void parse() {
		addIfNotPresent(CatalogueDBAdapter.KEY_TITLE, parseTitle());
		for (String isbn : parseISBN()) {
			if (!mValues.containsKey(CatalogueDBAdapter.KEY_ISBN) || isbn.length() > mValues.getString(CatalogueDBAdapter.KEY_ISBN).length()) {
				mValues.putString(CatalogueDBAdapter.KEY_ISBN, isbn);
			}
		}
		for (String author : parseAuthorDetails()) {
			Utils.appendOrAdd(mValues, CatalogueDBAdapter.KEY_AUTHOR_DETAILS, author);
		}
		addIfNotPresent(CatalogueDBAdapter.KEY_PUBLISHER, parsePublisher());
		mValues.putString(CatalogueDBAdapter.KEY_PAGES, parsePages());
		for (String ganre : parseGenres()) {
			mValues.putString(CatalogueDBAdapter.KEY_GENRE, ganre);
		}
		addIfNotPresent(CatalogueDBAdapter.KEY_DESCRIPTION, parseDescription());
		String thumbnail = parseThumbnail();
		String filename = Utils.saveThumbnailFromUrl(thumbnail, "_YES24");
		if (filename.length() > 0) {
			Utils.appendOrAdd(mValues, "__thumbnail", filename);
		}
	}

	private String parseTitle() {
		return mDocument.select("a.nm_book_title").get(0).text();
	}

	private List<String> parseISBN() {
		List<String> list= new ArrayList<String>(2);
		list.add(mDocument.getElementById("hd_ISBN").attr("value"));
		list.add(
			mDocument.select("#m_product_head1_txt_PackingInfo li")
			         .get(1)
			         .text()
			         .replaceAll("^ISBN(.+) : ", "")
		);
		return list;
	}

	private List<String> parseAuthorDetails() {
		List<String> list= new ArrayList<String>(1);
		list.add(mDocument.getElementById("m_product_authorInfo1_txtAuthorInfo")
				          .text()
				          .replaceAll(" \\(지은이\\)", ""));
		return list;
	}

	private String parsePublisher() {
		return mDocument.select(".nm_book_title_s a").get(1).text();
	}

	private String parsePages() {
		Elements elements = mDocument.select("#m_product_head1_txt_PackingInfo li");
		if (elements.size() == 0)
			return "";
		String sizes = elements.get(0).text();
		for (String size : sizes.split(" | ")) {
			if (size.endsWith("쪽")) {
				return size.replace("쪽", "");
			}
		}
		return "";
	}

	private List<String> parseGenres() {
		List<String> list = new ArrayList<String>(16);
		Elements elements = mDocument.select(".book_cb2 .book_conts2 li");
		for (Element element : elements) {
			list.add(element.text().replaceAll("/", "&").replaceAll("&nbsp;>&nbsp;", " / "));
		}
		return list;
	}

	private String parseDescription() {
		String description = "";
		try {
			description = mDocument.getElementById("Description").text();
		} catch (NullPointerException e) {
		}
		return description.replaceAll("<BR>", "\n");
	}

	private String parseThumbnail() {
		return mDocument.select(".mp_book_img img").get(0).attr("src");
	}

}