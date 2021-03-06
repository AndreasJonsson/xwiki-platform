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
package org.xwiki.administration.test.po;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * Represents the actions possible on the Templates Administration Page.
 * 
 * @version $Id$
 * @since 4.2M1
 */
public class TemplatesAdministrationSectionPage extends AdministrationSectionPage
{
    public static final String ADMINISTRATION_SECTION_ID = "Templates";

    @FindBy(id = "space")
    private WebElement spaceInput;

    @FindBy(id = "page")
    private WebElement pageInput;

    @FindBy(id = "createTemplateProvider")
    private WebElement createButton;

    /**
     * @since 4.2M1
     */
    public static TemplatesAdministrationSectionPage gotoPage()
    {
        AdministrationSectionPage.gotoPage(ADMINISTRATION_SECTION_ID);
        return new TemplatesAdministrationSectionPage();
    }

    public TemplatesAdministrationSectionPage()
    {
        super(ADMINISTRATION_SECTION_ID);
    }

    public TemplateProviderInlinePage createTemplateProvider(String space, String page)
    {
        this.spaceInput.clear();
        this.spaceInput.sendKeys(space);
        this.pageInput.clear();
        this.pageInput.sendKeys(page);
        this.createButton.click();
        return new TemplateProviderInlinePage();
    }

    public List<WebElement> getExistingTemplatesLinks()
    {
        // A bit unreliable here, but it's the best I can do.
        return getDriver().findElements(By.xpath("//ul[preceding-sibling::*[. = 'Available Template Providers']]//a"));
    }
}
