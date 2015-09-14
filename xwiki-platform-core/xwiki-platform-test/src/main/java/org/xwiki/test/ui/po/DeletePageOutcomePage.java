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
package org.xwiki.test.ui.po;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * Represents the common actions possible after a page has been deleted.
 * 
 * @version $Id$
 * @since 3.2M3
 */
public class DeletePageOutcomePage extends ViewPage
{
    /**
     * The Deleter is found in the first cell of the first column of the table listing past deletions.
     * Note that when the user has no profile (as with superadmin for example or if a user's profile has been
     * removed) then the deleted is not wrapped in an A tag and thus we shouldn't use that as a way to locate
     * the deleter.
     */
    @FindBy(xpath = "//table[@class='centered']/tbody/tr/td")
    private WebElement deleter;

    @FindBy(xpath = "//p[@class='xwikimessage']")
    private WebElement message;

    @FindBy(xpath = "//*[@id = 'mainContentArea']//a[. = 'Restore']")
    private WebElement restoreLink;

    /**
     * @since 3.2M3
     */
    public String getPageDeleter()
    {
        return this.deleter.getText();
    }

    /**
     * @since 4.0M2
     */
    public String getMessage()
    {
        return this.message.getText();
    }

    /**
     * Clicks on the link to restore the deleted page from the recycle bin.
     * 
     * @return the restored view page
     * @since 5.2M2
     */
    public ViewPage clickRestore()
    {
        this.restoreLink.click();
        return new ViewPage();
    }
}
