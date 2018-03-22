package cz.vsb.cs.neurace.gui;

import cz.vsb.cs.neurace.gui.track.TrackEdit;
import javax.swing.SpinnerNumberModel;


/**
 *
 * @author Petr Hamalcik
 */
public class WallPanel extends javax.swing.JPanel {

    /**
     * Creates new form WallPanel
     */
    public WallPanel() {
        initComponents();
    }
    
    private TrackEdit trackEdit;

    public void setTrackEdit(TrackEdit trackEdit) {
        this.trackEdit = trackEdit;
    }
    
    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        wallButton = new javax.swing.JButton();
        widthSpinner = new javax.swing.JSpinner();
        widthLabel = new javax.swing.JLabel();
        heightLabel = new javax.swing.JLabel();
        heightSpinner = new javax.swing.JSpinner();
        jLabel1 = new javax.swing.JLabel();

        setMaximumSize(getPreferredSize());
        setPreferredSize(new java.awt.Dimension(150, 150));

        wallButton.setText(Text.getString("createWall")); // NOI18N
        wallButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                wallButtonActionPerformed(evt);
            }
        });

        widthSpinner.setModel(new SpinnerNumberModel(Float.valueOf(1.0f), Float.valueOf(0.1f), Float.valueOf(20.0f), Float.valueOf(0.5f)));

        widthLabel.setText(Text.getString("width")); // NOI18N

        heightLabel.setText(Text.getString("height")); // NOI18N

        heightSpinner.setModel(new SpinnerNumberModel(Float.valueOf(1.0f), Float.valueOf(0.1f), Float.valueOf(20.0f), Float.valueOf(0.5f)));

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText(Text.getString("wall")); // NOI18N
        jLabel1.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(180, 180, 180), 1, true));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(wallButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(widthLabel)
                        .addGap(31, 31, 31)
                        .addComponent(widthSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, 71, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(heightLabel)
                        .addGap(28, 28, 28)
                        .addComponent(heightSpinner)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(widthSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(widthLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(heightLabel)
                    .addComponent(heightSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(wallButton)
                .addGap(34, 34, 34))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void wallButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_wallButtonActionPerformed
        if(trackEdit != null) {
            SpinnerNumberModel heightModel = (SpinnerNumberModel)heightSpinner.getModel();
            SpinnerNumberModel widthModel = (SpinnerNumberModel)widthSpinner.getModel();
            float height = heightModel.getNumber().floatValue();
            float width = widthModel.getNumber().floatValue();
            trackEdit.setWallMode(height, width);
        }
    }//GEN-LAST:event_wallButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel heightLabel;
    private javax.swing.JSpinner heightSpinner;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JButton wallButton;
    private javax.swing.JLabel widthLabel;
    private javax.swing.JSpinner widthSpinner;
    // End of variables declaration//GEN-END:variables
}
