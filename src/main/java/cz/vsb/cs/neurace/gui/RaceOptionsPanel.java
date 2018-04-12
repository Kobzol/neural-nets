package cz.vsb.cs.neurace.gui;

import cz.vsb.cs.neurace.gui.track.RaceView;

/**
 * Panel s nastavením zobrazení závodu.
 * @author Petr Hamalcik
 */
public class RaceOptionsPanel extends javax.swing.JPanel {

    private RaceView racePaint;
    private ToggleListener listener;
    
    /**
     * Creates new form RaceOptionsPanel
     */
    public RaceOptionsPanel(RaceView racePaint) {
        initComponents();
        this.racePaint = racePaint;
        setSelected();
       
    }
    
    public final void setSelected() {
        arrowCheck.setSelected(racePaint.getDrawArrow());
        namesCheck.setSelected(racePaint.getDrawNames());
        outlinesCheck.setSelected(racePaint.getDrawOutline());
        sensorsCheck.setSelected(racePaint.getDrawSensor());
        texturesCheck.setSelected(racePaint.getUseTextures());
        wheelsCheck.setSelected(racePaint.getDrawWheelsOver());
    }

    public void setToggleButtonListener(ToggleListener listener) {
        this.listener = listener;
    }
    
    
    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        texturesCheck = new javax.swing.JCheckBox();
        namesCheck = new javax.swing.JCheckBox();
        arrowCheck = new javax.swing.JCheckBox();
        wheelsCheck = new javax.swing.JCheckBox();
        sensorsCheck = new javax.swing.JCheckBox();
        sensorsBlockCheck = new javax.swing.JCheckBox();
        sensorsForAllCarsCheck = new javax.swing.JCheckBox();
        outlinesCheck = new javax.swing.JCheckBox();
        closeButton = new javax.swing.JButton();
        saveButton = new javax.swing.JButton();

        setBorder(new javax.swing.border.LineBorder(new java.awt.Color(105, 105, 105), 1, true));

        texturesCheck.setText(Text.getString("textures")); // NOI18N
        texturesCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                texturesCheckActionPerformed(evt);
            }
        });

        namesCheck.setText(Text.getString("names")); // NOI18N
        namesCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                namesCheckActionPerformed(evt);
            }
        });

        arrowCheck.setText(Text.getString("arrow")); // NOI18N
        arrowCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                arrowCheckActionPerformed(evt);
            }
        });

        wheelsCheck.setText(Text.getString("wheels")); // NOI18N
        wheelsCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                wheelsCheckActionPerformed(evt);
            }
        });

        sensorsCheck.setText(Text.getString("sensors")); // NOI18N
        sensorsCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sensorsCheckActionPerformed(evt);
            }
        });
        sensorsBlockCheck.setText(Text.getString("sensorsBlock")); // NOI18N
        sensorsBlockCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                racePaint.setDrawSensorBlock(sensorsBlockCheck.isSelected());
            }
        });
        sensorsForAllCarsCheck.setText(Text.getString("sensorsForAll")); // NOI18N
        sensorsForAllCarsCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                racePaint.setDrawSensorForAllCars(sensorsForAllCarsCheck.isSelected());
            }
        });

        outlinesCheck.setText(Text.getString("outlines")); // NOI18N
        outlinesCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                outlinesCheckActionPerformed(evt);
            }
        });

        closeButton.setText(Text.getString("close")); // NOI18N
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        saveButton.setText(Text.getString("save")); // NOI18N
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(closeButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(saveButton))
                    .addComponent(sensorsCheck)
                    .addComponent(sensorsBlockCheck)
                    .addComponent(sensorsForAllCarsCheck)
                    .addComponent(namesCheck)
                    .addComponent(wheelsCheck)
                    .addComponent(outlinesCheck)
                    .addComponent(texturesCheck)
                    .addComponent(arrowCheck))
                .addContainerGap(11, Short.MAX_VALUE))
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {closeButton, saveButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(arrowCheck)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(namesCheck)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(outlinesCheck)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(sensorsCheck)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(sensorsBlockCheck)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(sensorsForAllCarsCheck)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(texturesCheck)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(wheelsCheck)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(closeButton)
                    .addComponent(saveButton))
                .addContainerGap(15, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void outlinesCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_outlinesCheckActionPerformed
        racePaint.setDrawOutline(outlinesCheck.isSelected());
    }//GEN-LAST:event_outlinesCheckActionPerformed

    private void arrowCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_arrowCheckActionPerformed
        racePaint.setDrawArrow(arrowCheck.isSelected());
    }//GEN-LAST:event_arrowCheckActionPerformed

    private void namesCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_namesCheckActionPerformed
        racePaint.setDrawNames(namesCheck.isSelected());
    }//GEN-LAST:event_namesCheckActionPerformed

    private void sensorsCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sensorsCheckActionPerformed
        racePaint.setDrawSensor(sensorsCheck.isSelected());
    }//GEN-LAST:event_sensorsCheckActionPerformed

    private void texturesCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_texturesCheckActionPerformed
        racePaint.setUseTextures(texturesCheck.isSelected());
    }//GEN-LAST:event_texturesCheckActionPerformed

    private void wheelsCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_wheelsCheckActionPerformed
        racePaint.setDrawWheelsOver(wheelsCheck.isSelected());
    }//GEN-LAST:event_wheelsCheckActionPerformed

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        racePaint.setOptionsPanel(null);
        this.getParent().remove(this);
        if(listener != null) {
            listener.toggleButton(ToggleListener.ButtonType.OPTIONS);
        }
    }//GEN-LAST:event_closeButtonActionPerformed

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        Config.get().set("arrow", arrowCheck.isSelected());
        Config.get().set("names", namesCheck.isSelected());
        Config.get().set("outlines", outlinesCheck.isSelected());
        Config.get().set("sensors", sensorsCheck.isSelected());
        Config.get().set("textures", texturesCheck.isSelected());
        Config.get().set("wheels", wheelsCheck.isSelected());
    }//GEN-LAST:event_saveButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox arrowCheck;
    private javax.swing.JButton closeButton;
    private javax.swing.JCheckBox namesCheck;
    private javax.swing.JCheckBox outlinesCheck;
    private javax.swing.JButton saveButton;
    private javax.swing.JCheckBox sensorsCheck;
    private javax.swing.JCheckBox sensorsBlockCheck;
    private javax.swing.JCheckBox sensorsForAllCarsCheck;
    private javax.swing.JCheckBox texturesCheck;
    private javax.swing.JCheckBox wheelsCheck;
    // End of variables declaration//GEN-END:variables
}