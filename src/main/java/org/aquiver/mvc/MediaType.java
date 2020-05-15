/*
 * MIT License
 *
 * Copyright (c) 2019 1619kHz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.aquiver.mvc;

public interface MediaType {
  String ALL_VALUE = "*/*";
  String APPLICATION_ATOM_XML_VALUE = "application/atom+xml";
  String APPLICATION_CBOR_VALUE = "application/cbor";
  String APPLICATION_FORM_URLENCODED_VALUE = "application/x-www-form-urlencoded";
  String APPLICATION_JSON_VALUE = "application/json";
  String APPLICATION_JSON_UTF8_VALUE = "application/json;charset=UTF-8";
  String APPLICATION_OCTET_STREAM_VALUE = "application/octet-stream";
  String APPLICATION_PDF_VALUE = "application/pdf";
  String APPLICATION_PROBLEM_JSON_VALUE = "application/problem+json";
  String APPLICATION_PROBLEM_JSON_UTF8_VALUE = "application/problem+json;charset=UTF-8";
  String APPLICATION_PROBLEM_XML_VALUE = "application/problem+xml";
  String APPLICATION_RSS_XML_VALUE = "application/rss+xml";
  String APPLICATION_STREAM_JSON_VALUE = "application/stream+json";
  String APPLICATION_XHTML_XML_VALUE = "application/xhtml+xml";
  String APPLICATION_XML_VALUE = "application/xml";
  String IMAGE_GIF_VALUE = "image/gif";
  String IMAGE_JPEG_VALUE = "image/jpeg";
  String IMAGE_PNG_VALUE = "image/png";
  String MULTIPART_FORM_DATA_VALUE = "multipart/form-data";
  String MULTIPART_MIXED_VALUE = "multipart/mixed";
  String MULTIPART_RELATED_VALUE = "multipart/related";
  String TEXT_EVENT_STREAM_VALUE = "text/event-stream";
  String TEXT_HTML_VALUE = "text/html";
  String TEXT_MARKDOWN_VALUE = "text/markdown";
  String TEXT_PLAIN_VALUE = "text/plain";
  String TEXT_XML_VALUE = "text/xml";
  String PARAM_QUALITY_FACTOR = "q";
}
