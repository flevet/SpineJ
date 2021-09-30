/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package SpineJ.Widgets;

import SpineJ.Tools.GUI.Packer;
import java.awt.ComponentOrientation;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

/**
 *
 * @author Florian Levet
 */
public class ReconnectionDialog extends Frame implements ActionListener{
    protected SpineReconnectionCanvas m_canvas = null;
    protected JRadioButton m_originalRadio = null, m_binarizedRadio = null, m_forceRadio = null;
    protected JButton m_undo = null, m_continue = null;
    
    public ReconnectionDialog( SpineReconnectionCanvas _canvas ){
        m_canvas = _canvas;

        ButtonGroup imageGroup = new ButtonGroup();
        JPanel imagePanel = new JPanel();
        imagePanel.applyComponentOrientation( ComponentOrientation.LEFT_TO_RIGHT );
        imagePanel.setBorder( BorderFactory.createTitledBorder( "Display Image" ) );
        Packer pkImage = new Packer( imagePanel );
        m_originalRadio = new JRadioButton();
        imageGroup.add( m_originalRadio );
        m_originalRadio.setText( "Original" );
        m_originalRadio.setSelected( false );
        m_binarizedRadio = new JRadioButton();
        imageGroup.add( m_binarizedRadio );
        m_binarizedRadio.setText( "Binarized" );
        m_binarizedRadio.setSelected( true );
        pkImage.pack( m_originalRadio ).gridx( 0 ).gridy( 0 ).fillx().inset( 3, 3, 3, 3 );
        pkImage.pack( m_binarizedRadio ).gridx( 0 ).gridy( 1 ).fillx().inset( 3, 3, 3, 3 );
        
        JPanel forcePanel = new JPanel();
        forcePanel.applyComponentOrientation( ComponentOrientation.LEFT_TO_RIGHT );
        forcePanel.setBorder( BorderFactory.createTitledBorder( "Force reconnection" ) );
        Packer pkForce = new Packer( forcePanel );
        m_forceRadio = new JRadioButton();
        m_forceRadio.setText( "On" );
        m_forceRadio.setSelected( false );
        pkForce.pack( m_forceRadio ).gridx( 0 ).gridy( 0 ).fillx().inset( 3, 3, 3, 3 );
        
        m_undo = new JButton( "Undo" );
        m_continue = new JButton( "Continue" );

        JPanel panel = new JPanel();
        Packer pk = new Packer( panel );
        pk.pack( imagePanel ).gridx( 0 ).gridy( 0 ).fillboth().inset( 3, 3, 3, 3 );
        pk.pack( forcePanel ).gridx( 0 ).gridy( 1 ).fillboth().inset( 3, 3, 3, 3 );
        pk.pack( m_undo ).gridx( 0 ).gridy( 2 ).fillboth().inset( 3, 3, 3, 3 );
        pk.pack( m_continue ).gridx( 0 ).gridy( 3 ).fillboth().inset( 3, 3, 3, 3 );
        
        m_originalRadio.addActionListener( this );
        m_binarizedRadio.addActionListener( this );
        m_forceRadio.addActionListener( this );
        m_undo.addActionListener( this );
        
        this.add( panel );
        this.setSize( 250, 300 );
        this.setTitle( "Reconnection Dialog" );
        this.setVisible(true);
    }
    
    public JButton getContinueButton(){
        return m_continue;
    }

    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();
        if( o instanceof JRadioButton ){
            if( o == m_originalRadio )
                m_canvas.setOriginalImage();
            else if(o == m_binarizedRadio)
                m_canvas.setBinarizedImage();
            else if( o == m_forceRadio )
                m_canvas.setForceIsActivated( m_forceRadio.isSelected() );
        }
        else if(o instanceof JButton)
        {
            if( o == m_undo ){
                m_canvas.undoReconnection();
            }
        }
    }
}
