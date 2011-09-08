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
 * An HTML handler for the Yes24 Books entry return 
 * 
 * ...
 * <html>
 * <HEAD><base href="http://www.yes24.com/24/" />
 * <!-- Facebook Interface -->
 * <meta property="og:title" content="Harry Potter and the Sorcerer's Stone"/>
 * <meta property="og:type" content="book"/>
 * <meta property="og:url" content="http://www.yes24.com/24/goods/294819"/>
 * <meta property="og:image" content="http://image.yes24.com/goods/294819/M"/>
 * <meta property="og:site_name" content="YES24"/>
 * <meta property="fb:admins" content="112933415429029"/>
 * <meta property="og:description" content="더이상&nbsp;말이&nbsp;필요없는&nbsp;전세계의&nbsp;베스트셀러,&nbsp;해리포터&nbsp;시리즈의&nbsp;미국판&nbsp;원서&nbsp;1편.&nbsp;어려서&nbsp;부모님이&nbsp;돌아가시고&nbsp;이모댁에서&nbsp;온갖&nbsp;구박을&nbsp;받으며&nbsp;자라던&nbsp;고아&nbsp;소년&nbsp;해리포터는&nbsp;어느&nbsp;날&nbsp;마법&nbsp;학교로부터의&nbsp;입학통지서를&nbsp;받는다.&nbsp;꼬마&nbsp;마법사&nbsp;해리가&nbsp;호그와트&nbsp;마법&nbsp;학교에&nbsp;입학하고&nbsp;벌어지는&nbsp;일련의&nbsp;사건들이&nbsp;기상천외하게&nbsp;펼쳐진다.&nbsp;우리는&nbsp;알지&nbsp;못하고&nbsp;있던&nbsp;마법사들의&nbsp;세계가&nbsp;실..."/>
 * ...
 * <div id="title">			
 *   <span class="rkey"><span class="rkeyL">외서</span></span>
 *   <h1><a href = "/24/goods/294819">Harry Potter and the Sorcerer's Stone</a></h1>
 *   	<span class="subtitle"> : Book 1</span><span class="feature"><span class="featureL">Paperback, 미국판</span></span>
 *      <span class="bullet"><img src="http://image.yes24.com/sysimage/categoryN/i_hrecomm.gif" alt="강력추천" title="강력추천">&nbsp;</span>
 *      <p>
 *        <a href="http://www.yes24.com//SearchCorner/Result?domain=ALL&author_yn=Y&query=J.+K.+Rowling">J. K. Rowling</a>,
 *        <a href="http://www.yes24.com//SearchCorner/Result?domain=ALL&author_yn=Y&query=Mary+GrandPre">Mary GrandPre</a> |
 *        <a href="http://www.yes24.com//SearchCorner/Result?domain=ALL&company_yn=Y&query=Arthur+A.+Levine+Books">Arthur A. Levine Books</a> |
 *        번역서 : 해리포터와 마법사의 돌
 *      </p>
 * ...
 * <div id="mainPic">
 *   <!--// 상품 사진 -->
 *   <div class="pic">
 *     <img id="mainImage"
 *          title="Harry Potter and the Sorcerer's Stone"
 *          alt="Harry Potter and the Sorcerer's Stone"
 *          src="http://image.yes24.com/momo/TopCate82/MidCate04/8134806.jpg"
 *     />
 *   </div>
 *   <!-- 상품 사진 //-->
 * ...
 * <div id="salsInfo">
 *   <input type="hidden" name = "Goods_Price" value="6500.00" />
 *   <!--// 가격 정보 박스 -->
 *   <div class="priceBox">
 *     <dl>
 *       <dt>정가</dt>
 *       <dd>14,300원</dd>
 *       <dt>판매가</dt>
 *       <dd><span class="price"><strong>6,500원</strong>(55% 할인)</span>
 *       ...
 *       <dt>출간일</dt>
 *       <dd class="pdDate">
 *         <p>1999년 09월 01일</p>      
 *         <p class="pdSize"> 312쪽 | 210g | 133*193mm</p>
 *       </dd>
 *       <dt>ISBN-13</dt>
 *       <dd class="isbn10">
 *         <p>9780590353427</p>
 *         <p class="isbn10"><span>ISBN-10</span>059035342X</p>
 *       </dd>
 * ...
 * <a name = "contentsIntro"></a>
 * <div class="communtyHide">
 *   <h2><img title="책소개" alt="책소개" src="http://image.yes24.com/sysimage/detailN/st_introduceBook.gif"/></h2>
 *   <p>
 *     더이상 말이 필요없는 전세계의 베스트셀러, 해리포터 시리즈의 미국판 원서 1편. 어려서 부모님이 돌아가시고 이모댁에서 온갖 구박을 받으며 자라던 고아 소년 해리포터는 어느 날 마법 학교로부터의 입학통지서를 받는다. 꼬마 마법사 해리가 호그와트 마법 학교에 입학하고 벌어지는 일련의 사건들이 기상천외하게 펼쳐진다. 우리는 알지 못하고 있던 마법사들의 세계가 실제로 있는 것은 아닌가, 의구심을 갖게 할만큼 놀라운 상상력을 보여주는 책.
 *   </p>
 *   <p>
 *     This story is filled with dark comedy and crafted with a quality of writing that has garnered J.K. Rowling top awards in her country and ours. Harry Potter spent ten long years living with his aunt and uncle and stupid cousin, Dudley. Fortunately, Harry has a destiny that he was born to fulfill. One that will rocket him out of his dull life and into a unique experience at the Hogworts School of Witchcraft and Wizardry.
 *   </p>
 * ...
 * 
 */

public class SearchYes24BooksEntryHandler {
	private Document mDocument = null;
	private Bundle mValues;
	private boolean mFetchThumbnail;
	private String title = "";
	private String thumbnail = "";
	private String description = "";
	
	public SearchYes24BooksEntryHandler(String url, Bundle values, boolean fetchThumbnail) {
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
		parseMetaData();
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
		addIfNotPresent(CatalogueDBAdapter.KEY_DATE_PUBLISHED, parseDatePublished());
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

	private void parseMetaData() {
		Elements elements = mDocument.select("meta");
		for (Element element : elements) {
			if (!element.hasAttr("property") || !element.hasAttr("content")) {
				continue;
			}
			String property = element.attr("property");
			String content = element.attr("content");
			if (property.equalsIgnoreCase("og:title")) {
				title = content;
			} else if (mFetchThumbnail && property.equalsIgnoreCase("og:image")) {
				thumbnail = content;
			} else if (property.equalsIgnoreCase("og:description")) {
				description = content;
			}
		}
	}

	private String parseTitle() {
		return title;
	}

	private List<String> parseISBN() {
		List<String> list= new ArrayList<String>(2);
		Elements elements = mDocument.select(".isbn10");
		if (elements.size() > 0) {
			String isbns = elements.get(0).text();
			for (String isbn : isbns.split(" ")) {
				list.add(isbn.replaceAll("^ISBN-10", ""));
			}
		}
		return list;
	}

	private List<String> parseAuthorDetails() {
		List<String> list= new ArrayList<String>(1);
		Elements elements = mDocument.select("#title p a");
		if (elements.size() > 0) {
			list.add(elements.get(0).text());
		}
		return list;
	}

	private String parsePublisher() {
		Elements elements = mDocument.select("#title p a");
		if (elements.size() > 1) {
			return elements.get(1).text();
		}
		return "";
	}

	private String parseDatePublished() {
		Elements elements = mDocument.select(".pdDate p");
		if (elements.size() > 0) {
			String date = elements.get(0).text();
			return date.replaceAll(" ", "-").replaceAll("[^0-9-]", "");
		}
		return "";
	}

	private String parsePages() {
		Elements elements = mDocument.select(".pdSize");
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
		Elements elements = mDocument.select(".basicListType.communtyHide li");
		for (Element element : elements) {
			list.add(element.text().replaceAll("/", "&").replaceAll(" > ", " / "));
		}
		return list;
	}

	private String parseDescription() {
		return description;
	}

	private String parseThumbnail() {
		return thumbnail;
	}

}
