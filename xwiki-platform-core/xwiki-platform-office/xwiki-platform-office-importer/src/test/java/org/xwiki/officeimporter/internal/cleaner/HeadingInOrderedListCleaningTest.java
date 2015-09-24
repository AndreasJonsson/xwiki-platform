/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.officeimporter.internal.cleaner;

import java.io.StringReader;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.junit.Assert;
import org.junit.Test;


public class HeadingInOrderedListCleaningTest extends AbstractHTMLCleaningTest
{

    @Test
    public void testHeadingInOrderedList()
    {
        assertCleanHTML("<ol><li><br><ol><li><br><ol><li><br><ol><li><div><h4>text</h4></div></li></ol></li></ol></li></ol></li></ol>",
                        "<h4>text</h4>"
                        );

        assertCleanHTML("<ol><li><br><ol><li><div><h2>text</h2></div></li></ol></li></ol>", "<h2>text</h2>");

        assertCleanHTML("<ol><li><br><ol><li><br><ol><li><div><h3>text</h3></div></li></ol></li></ol></li></ol>",
                        "<h3>text</h3>");

        assertCleanHTML("<ol><li><br><ol><li><br><ol><li><div><h3>h3 text</h3><ol><li><br><ol><li></li></ol></li></ol><h5>h5 <ins>ins</ins> text</h5><table><tbody><tr><td></td><td>td text</td></tr><tr><td></td><td><div><p>p1</p><p>p2</p></div></td></tr></tbody></table><ol><li><br><ol><li></li></ol></li></ol><h5>h5 <ins>ins</ins> text</h5><table><tbody><tr><td></td><td><div><p>p4</p><p>p5</p><p>p6</p></div></td></tr><tr><td colspan=\"2\"><div><p>p7</p><p>p8</p><p>p9</p></div></td></tr></tbody></table></div></li></ol></li></ol></li><li><div><h1>h1 text</h1></div></li></ol>", "<h3>h3 text</h3><h5>h5 <ins>ins</ins> text</h5><table><tbody><tr><td></td><td>td text</td></tr><tr><td></td><td><p>p1</p><p>p2</p></td></tr></tbody></table><h5>h5 <ins>ins</ins> text</h5><table><tbody><tr><td></td><td><p>p4</p><p>p5</p><p>p6</p></td></tr><tr><td colspan=\"2\"><p>p7</p><p>p8</p><p>p9</p></td></tr></tbody></table><h1>h1 text</h1>");
    }



}
