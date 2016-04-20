/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

/**
 * GUI class supporting the MD5Hex assertion functionality.
 *
 */
package org.apache.jmeter.assertions.gui;

import java.awt.BorderLayout;

import javax.swing.*;

import org.apache.jmeter.assertions.P2PAssertion;
import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;

public class P2PAssertionGUI extends AbstractAssertionGui {

    private static final long serialVersionUID = 240L;

    private JTextArea p2pTcpInput;

    private JTextField propertyInput;

    private JRadioButton resBox;

    private JRadioButton equalBox;

    public P2PAssertionGUI() {
        init();
    }

    private void init() {

        setLayout(new BorderLayout(0, 10));
        setBorder(makeBorder());

        add(makeTitlePanel(), BorderLayout.NORTH);

        JPanel mainPanel = new JPanel(new BorderLayout());

        // USER_INPUT
        HorizontalPanel md5HexPanel = new HorizontalPanel();
        md5HexPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                JMeterUtils.getResString("P2P_assertion_md5hex_test"))); // $NON-NLS-1$

        md5HexPanel.add(new JLabel(JMeterUtils.getResString("P2P_assertion_label"))); //$NON-NLS-1$

        p2pTcpInput = new JTextArea(15,80);
        // md5HexInput.addFocusListener(this);
        md5HexPanel.add(p2pTcpInput);

        //
        HorizontalPanel propertyPanel = new HorizontalPanel();
        propertyPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                "Property Save to vars(comma-delimited)")); // $NON-NLS-1$

        propertyPanel.add(new JLabel("Property")); //$NON-NLS-1$

        propertyInput = new JTextField(25);
        // md5HexInput.addFocusListener(this);
        propertyPanel.add(propertyInput);

        mainPanel.add(md5HexPanel, BorderLayout.CENTER);
        mainPanel.add(propertyPanel, BorderLayout.SOUTH);
        add(mainPanel, BorderLayout.CENTER);

    }

    @Override
    public void configure(TestElement el) {
        super.configure(el);
        P2PAssertion assertion = (P2PAssertion) el;
        this.p2pTcpInput.setText(String.valueOf(assertion.getP2PTxt()));
        this.propertyInput.setText(String.valueOf(assertion.getPropertyTxt()));
    }

    @Override
    public String getLabelResource() {
        return "P2P_assertion_title"; // $NON-NLS-1$
    }

    /*
     * @return
     */
    @Override
    public TestElement createTestElement() {

        P2PAssertion el = new P2PAssertion();
        modifyTestElement(el);
        return el;

    }

    /*
     * @param element
     */
    @Override
    public void modifyTestElement(TestElement element) {
        configureTestElement(element);
        String md5HexString = this.p2pTcpInput.getText();
        // initialize to empty string, this will fail the assertion
        if (md5HexString == null || md5HexString.length() == 0) {
            md5HexString = "";
        }
        ((P2PAssertion) element).setP2PTxt(md5HexString);

        String propertyString = this.propertyInput.getText();
        // initialize to empty string, this will fail the assertion
        if (propertyString == null || propertyString.length() == 0) {
            propertyString = "";
        }
        ((P2PAssertion) element).setPropertyTxt(propertyString);
    }

    /**
     * Implements JMeterGUIComponent.clearGui
     */
    @Override
    public void clearGui() {
        super.clearGui();

        p2pTcpInput.setText(""); //$NON-NLS-1$
        propertyInput.setText("");
    }
}
