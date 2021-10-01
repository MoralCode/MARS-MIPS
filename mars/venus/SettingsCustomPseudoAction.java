   package mars.venus;

   import mars.Globals;
   import mars.Settings;
   import mars.venus.editors.jeditsyntax.SyntaxStyle;
   import mars.venus.editors.jeditsyntax.SyntaxUtilities;
   import mars.venus.editors.jeditsyntax.tokenmarker.MIPSTokenMarker;

   import javax.swing.*;
   import javax.swing.border.BevelBorder;
   import javax.swing.border.Border;
   import javax.swing.border.LineBorder;
   import javax.swing.event.ChangeEvent;
   import javax.swing.event.ChangeListener;
   import javax.swing.text.Caret;
   import java.awt.*;
   import java.awt.event.*;
	
	/*
Copyright (c) 2021, Adrian Edwards

Permission is hereby granted, free of charge, to any person obtaining 
a copy of this software and associated documentation files (the 
"Software"), to deal in the Software without restriction, including 
without limitation the rights to use, copy, modify, merge, publish, 
distribute, sublicense, and/or sell copies of the Software, and to 
permit persons to whom the Software is furnished to do so, subject 
to the following conditions:

The above copyright notice and this permission notice shall be 
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, 
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR 
ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION 
WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

(MIT license, http://www.opensource.org/licenses/mit-license.html)
 */

   /**
    * Action class for the Settings menu item for text editor settings.
    */
   public class SettingsCustomPseudoAction extends GuiAction  {

       JDialog editorDialog;

       // Used to determine upon OK, whether or not anything has changed.

       /**
        *  Create a new SettingsEditorAction.  Has all the GuiAction parameters.
        */
       public SettingsCustomPseudoAction(String name, Icon icon, String descrip,
                                         Integer mnemonic, KeyStroke accel, VenusUI gui) {
           super(name, icon, descrip, mnemonic, accel, gui);
       }

       /**
        *  When this action is triggered, launch a dialog to view and modify
        *  editor settings.
        */
       public void actionPerformed(ActionEvent e) {
           editorDialog = new EditPseudoOpsDialog(Globals.getGui(), "Pseudo operation Settings", true, Globals.getSettings().getPseudoOpsFilepath() );
           editorDialog.setVisible(true);

       }


       private static final String GENERIC_TOOL_TIP_TEXT = "Use generic editor (original MARS editor, similar to Notepad) instead of language-aware styled editor";

       private static final String SAMPLE_TOOL_TIP_TEXT = "Current setting; modify using buttons to the right";
       private static final String FOREGROUND_TOOL_TIP_TEXT = "Click, to select text color";
       private static final String BOLD_TOOL_TIP_TEXT = "Toggle text bold style";
       private static final String ITALIC_TOOL_TIP_TEXT = "Toggle text italic style";
       private static final String DEFAULT_TOOL_TIP_TEXT = "Check, to select defaults (disables buttons)";
       private static final String BOLD_BUTTON_TOOL_TIP_TEXT = "B";
       private static final String ITALIC_BUTTON_TOOL_TIP_TEXT = "I";

       private static final String TAB_SIZE_TOOL_TIP_TEXT = "Current tab size in characters";
       private static final String BLINK_SPINNER_TOOL_TIP_TEXT = "Current blinking rate in milliseconds";
       private static final String BLINK_SAMPLE_TOOL_TIP_TEXT = "Displays current blinking rate";
       private static final String CURRENT_LINE_HIGHLIGHT_TOOL_TIP_TEXT = "Check, to highlight line currently being edited";
       private static final String AUTO_INDENT_TOOL_TIP_TEXT = "Check, to enable auto-indent to previous line when Enter key is pressed";
       private static final String[] POPUP_GUIDANCE_TOOL_TIP_TEXT = { "Turns off instruction and directive guide popup while typing",
               "Generates instruction guide popup after first letter of potential instruction is typed",
               "Generates instruction guide popup after second letter of potential instruction is typed"
       };

       // Concrete font chooser class. 
       private class EditPseudoOpsDialog extends AbstractFontSettingDialog {

           private JButton[] foregroundButtons;
           private JLabel[] samples;
           private JToggleButton[] bold, italic;
           private JCheckBox[] useDefault;

           private int[] syntaxStyleIndex;
           private SyntaxStyle[] defaultStyles,initialStyles, currentStyles;
           private Font previewFont;

           private JPanel dialogPanel,syntaxStylePanel,otherSettingsPanel; /////4 Aug 2010

           private JSlider tabSizeSelector;
           private JSpinner tabSizeSpinSelector, blinkRateSpinSelector, popupPrefixLengthSpinSelector;
           private JCheckBox lineHighlightCheck, genericEditorCheck, autoIndentCheck;
           private Caret blinkCaret;
           private JTextField blinkSample;
           private ButtonGroup popupGuidanceButtons;
           private JRadioButton[] popupGuidanceOptions;
           // Flag to indicate whether any syntax style buttons have been clicked
           // since dialog created or most recent "apply".
           private boolean syntaxStylesAction = false;

           private int initialEditorTabSize, initialCaretBlinkRate, initialPopupGuidance;
           private boolean initialLineHighlighting, initialGenericTextEditor, initialAutoIndent;

           public EditPseudoOpsDialog(Frame owner, String title, boolean modality, Font font) {
               super(owner, title, modality, font);
               if (Globals.getSettings().getBooleanSetting(Settings.GENERIC_TEXT_EDITOR)) {
                   syntaxStylePanel.setVisible(false);
                   otherSettingsPanel.setVisible(false);
               }
           }

           // build the dialog here
           protected JPanel buildDialogPanel() {
               JPanel dialog = new JPanel(new BorderLayout());
               JPanel fontDialogPanel = super.buildDialogPanel();
               JPanel syntaxStylePanel = buildSyntaxStylePanel();
               JPanel otherSettingsPanel = buildOtherSettingsPanel();
               fontDialogPanel.setBorder(BorderFactory.createTitledBorder("Editor Font"));
               syntaxStylePanel.setBorder(BorderFactory.createTitledBorder("Syntax Styling"));
               otherSettingsPanel.setBorder(BorderFactory.createTitledBorder("Other Editor Settings"));
               dialog.add(fontDialogPanel, BorderLayout.WEST);
               dialog.add(syntaxStylePanel, BorderLayout.CENTER);
               dialog.add(otherSettingsPanel, BorderLayout.SOUTH);
               this.dialogPanel = dialog; /////4 Aug 2010
               return dialog;
           }

           // Row of control buttons to be placed along the button of the dialog
           protected Component buildControlPanel() {
               Box controlPanel = Box.createHorizontalBox();
               JButton okButton = new JButton("Apply and Close");
               okButton.setToolTipText(SettingsHighlightingAction.CLOSE_TOOL_TIP_TEXT);
               okButton.addActionListener(
                       new ActionListener() {
                           public void actionPerformed(ActionEvent e) {
                               performApply();
                               closeDialog();
                           }
                       });
               JButton applyButton = new JButton("Apply");
               applyButton.setToolTipText(SettingsHighlightingAction.APPLY_TOOL_TIP_TEXT);
               applyButton.addActionListener(
                       new ActionListener() {
                           public void actionPerformed(ActionEvent e) {
                               performApply();
                           }
                       });
               JButton cancelButton = new JButton("Cancel");
               cancelButton.setToolTipText(SettingsHighlightingAction.CANCEL_TOOL_TIP_TEXT);
               cancelButton.addActionListener(
                       new ActionListener() {
                           public void actionPerformed(ActionEvent e) {
                               closeDialog();
                           }
                       });
               JButton resetButton = new JButton("Reset");
               resetButton.setToolTipText(SettingsHighlightingAction.RESET_TOOL_TIP_TEXT);
               resetButton.addActionListener(
                       new ActionListener() {
                           public void actionPerformed(ActionEvent e) {
                               reset();
                           }
                       });
               initialGenericTextEditor = Globals.getSettings().getBooleanSetting(Settings.GENERIC_TEXT_EDITOR);
               genericEditorCheck = new JCheckBox("Use Generic Editor", initialGenericTextEditor);
               genericEditorCheck.setToolTipText(GENERIC_TOOL_TIP_TEXT);
               genericEditorCheck.addItemListener(
                       new ItemListener() {
                           public void itemStateChanged(ItemEvent e) {
                               if (e.getStateChange()==ItemEvent.SELECTED) {
                                   syntaxStylePanel.setVisible(false);
                                   otherSettingsPanel.setVisible(false);
                               }
                               else {
                                   syntaxStylePanel.setVisible(true);
                                   otherSettingsPanel.setVisible(true);
                               }
                           }
                       });

               controlPanel.add(Box.createHorizontalGlue());
               controlPanel.add(okButton);
               controlPanel.add(Box.createHorizontalGlue());
               controlPanel.add(applyButton);
               controlPanel.add(Box.createHorizontalGlue());
               controlPanel.add(cancelButton);
               controlPanel.add(Box.createHorizontalGlue());
               controlPanel.add(resetButton);
               controlPanel.add(Box.createHorizontalGlue());
               controlPanel.add(genericEditorCheck);
               controlPanel.add(Box.createHorizontalGlue());
               return controlPanel;
           }

           // User has clicked "Apply" or "Apply and Close" button.  Required method, is 
           // abstract in superclass.
           protected void apply(Font font) {
               Globals.getSettings().setBooleanSetting(Settings.GENERIC_TEXT_EDITOR, genericEditorCheck.isSelected());
               Globals.getSettings().setBooleanSetting(Settings.EDITOR_CURRENT_LINE_HIGHLIGHTING, lineHighlightCheck.isSelected());
               Globals.getSettings().setBooleanSetting(Settings.AUTO_INDENT, autoIndentCheck.isSelected());
               Globals.getSettings().setCaretBlinkRate(((Integer)blinkRateSpinSelector.getValue()).intValue());
               Globals.getSettings().setEditorTabSize(tabSizeSelector.getValue());
               if (syntaxStylesAction) {
                   for (int i=0; i<syntaxStyleIndex.length; i++) {
                       Globals.getSettings().setEditorSyntaxStyleByPosition( syntaxStyleIndex[i],
                               new SyntaxStyle(samples[i].getForeground(),
                                       italic[i].isSelected(), bold[i].isSelected()) );
                   }
                   syntaxStylesAction = false; // reset
               }
               Globals.getSettings().setEditorFont(font);
               for (int i=0; i<popupGuidanceOptions.length; i++) {
                   if (popupGuidanceOptions[i].isSelected()) {
                       if (i==0) {
                           Globals.getSettings().setBooleanSetting(Settings.POPUP_INSTRUCTION_GUIDANCE, false);
                       }
                       else {
                           Globals.getSettings().setBooleanSetting(Settings.POPUP_INSTRUCTION_GUIDANCE, true);
                           Globals.getSettings().setEditorPopupPrefixLength(i);
                       }
                       break;
                   }
               }
           }

           // User has clicked "Reset" button.  Put everything back to initial state.
           protected void reset() {
           }


           // Perform reset on miscellaneous editor settings
           private void resetOtherSettings() {
           }

           // Miscellaneous editor settings (cursor blinking, line highlighting, tab size, etc)


           // control style (color, plain/italic/bold) for syntax highlighting


           // Set or reset the changeable features of component for syntax style 


           // set the foreground color, bold and italic of sample (a JLabel)
           private void setSampleStyles(JLabel sample, SyntaxStyle style) {
               Font f = previewFont;
               if (style.isBold()) {
                   f = f.deriveFont(f.getStyle() ^ Font.BOLD);
               }
               if (style.isItalic()) {
                   f = f.deriveFont(f.getStyle() ^ Font.ITALIC);
               }
               sample.setFont(f);
               sample.setForeground(style.getColor());
           }


           ///////////////////////////////////////////////////////////////////////////
           // Toggle bold or italic style on preview button when B or I button clicked	

           /////////////////////////////////////////////////////////////////
           //
           //  Class that handles click on the foreground selection button
           //   		
           private class ForegroundChanger implements ActionListener {
               private int row;
               public ForegroundChanger(int pos) {
                   row = pos;
               }
               public void actionPerformed(ActionEvent e) {
                   JButton button = (JButton) e.getSource();
                   Color newColor = JColorChooser.showDialog(null, "Set Text Color", button.getBackground());
                   if (newColor != null) {
                       button.setBackground(newColor);
                       samples[row].setForeground(newColor);
                   }
                   currentStyles[row] = new SyntaxStyle(button.getBackground(),
                           italic[row].isSelected(), bold[row].isSelected());
                   syntaxStylesAction = true;
               }
           }

           /////////////////////////////////////////////////////////////////
           //
           // Class that handles action (check, uncheck) on the Default checkbox.
           //   	
           private class DefaultChanger implements ItemListener {
               private int row;
               public DefaultChanger(int pos) {
                   row = pos;
               }
               public void itemStateChanged(ItemEvent e) {

                   // If selected: disable buttons, save current settings, set to defaults
                   // If deselected:restore current settings, enable buttons
                   Color newBackground = null;
                   Font newFont = null;
                   if (e.getStateChange() == ItemEvent.SELECTED) {
                       foregroundButtons[row].setEnabled(false);
                       bold[row].setEnabled(false);
                       italic[row].setEnabled(false);
                       currentStyles[row] = new SyntaxStyle(foregroundButtons[row].getBackground(),
                               italic[row].isSelected(), bold[row].isSelected());
                       SyntaxStyle defaultStyle = defaultStyles[syntaxStyleIndex[row]];
                       setSampleStyles(samples[row], defaultStyle);
                       foregroundButtons[row].setBackground(defaultStyle.getColor());
                       bold[row].setSelected(defaultStyle.isBold());
                       italic[row].setSelected(defaultStyle.isItalic());
                   }
                   else {
                       setSampleStyles(samples[row], currentStyles[row]);
                       foregroundButtons[row].setBackground(currentStyles[row].getColor());
                       bold[row].setSelected(currentStyles[row].isBold());
                       italic[row].setSelected(currentStyles[row].isItalic());
                       foregroundButtons[row].setEnabled(true);
                       bold[row].setEnabled(true);
                       italic[row].setEnabled(true);
                   }
                   syntaxStylesAction = true;
               }
           }
       }

   }