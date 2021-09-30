/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package SpineJ.Tools.Wavelets;

import SpineJ.Tools.GUI.Packer;
import java.awt.ComponentOrientation;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.DecimalFormat;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

/**
 *
 * @author Florian Levet
 */
public class InfosPanel extends JPanel implements MouseListener{
    static public int ADDED = 0, REMOVED = 1, NONE = 2;
    ImageInfos ii = null;
    JLabel minLabel, maxLabel, meanLabel, noiseLabel;
    JTextField rapportField;
    JRadioButton m_plus, m_moins;
    static DecimalFormat df = new DecimalFormat( "#.##" );
    int m_currentState = NONE;

    public InfosPanel(){
        super();
    }
    
    public InfosPanel(int _state){
        m_currentState = _state;
    }

    public InfosPanel( ImageInfos _ii, int _index ){
        super();
        ii = _ii;

        minLabel = new JLabel();
        minLabel.setText( "Min: " + df.format( ii.getMin() ) );
        maxLabel = new JLabel();
        maxLabel.setText( "Max: " + df.format( ii.getMax() ) );
        meanLabel = new JLabel();
        meanLabel.setText( "Mean: " + df.format( ii.getMean() ) );
        JLabel rapportLabel = new JLabel();
        rapportLabel.setText( "Factor: " );
        rapportField = new JTextField();
        rapportField.setText( "" + ii.getRapportNoise() );
        m_plus = new JRadioButton();
        m_plus.setText( "+" );
        m_plus.setSelected( true );
        m_moins = new JRadioButton();
        m_moins.setText( "-" );
        m_moins.setSelected( false );

        m_plus.addMouseListener( this );
        m_moins.addMouseListener( this );

        this.applyComponentOrientation( ComponentOrientation.LEFT_TO_RIGHT );
        this.setBorder( BorderFactory.createTitledBorder( "Coefficients " + (_index+1) ) );
        Packer pk = new Packer( this );
        pk.pack( minLabel ).gridx( 0 ).gridy( 0 ).fillx().inset( 3, 3, 3, 3 );
        pk.pack( maxLabel ).gridx( 2 ).gridy( 0 ).fillx().inset( 3, 3, 3, 3 );
        pk.pack( m_plus ).gridx( 3 ).gridy( 0 ).fillx().inset( 3, 3, 3, 3 );
        pk.pack( meanLabel ).gridx( 0 ).gridy( 1 ).fillx().inset( 3, 3, 3, 3 );
        pk.pack( rapportLabel ).gridx( 1 ).gridy( 1 ).fillx().inset( 3, 3, 3, 3 );
        pk.pack( rapportField ).gridx( 2 ).gridy( 1 ).fillx().inset( 3, 3, 3, 3 );
        pk.pack( m_moins ).gridx( 3 ).gridy( 1 ).fillx().inset( 3, 3, 3, 3 );
    }

    public void setImageInfos( ImageInfos _ii ){
        ii = _ii;
    }

    public void addActionListener( ActionListener dialog ){
        rapportField.addActionListener( dialog );
        m_plus.addActionListener( dialog );
        m_moins.addActionListener( dialog );
    }
    public JTextField getTextField(){
        return rapportField;
    }
    public void setRapport( String _val ){
        rapportField.setText( _val );
    }

    public void setType( int _val ){
        m_currentState = _val;
        if( _val == ADDED ){
            m_plus.setSelected( true );
            m_moins.setSelected( false );
        }
        else if( _val == REMOVED ){
            m_plus.setSelected( false );
            m_moins.setSelected( true );
        }
        else{
            m_plus.setSelected( false );
            m_moins.setSelected( false );
        }
    }

    public boolean resultAdded(){
        return m_currentState == ADDED;
    }
    public boolean resultRemoved(){
        return m_currentState == REMOVED;
    }

    public void mousePressed(MouseEvent e) {
        Object o = e.getSource();
        if( o == m_plus ){
            if( !m_plus.isSelected() ){
                m_currentState = ADDED;
                m_moins.setSelected( false );
            }
            else
                m_currentState = NONE;
        }
        
        if( o == m_moins ){
            if( !m_moins.isSelected() ){
                m_currentState = REMOVED;
                m_plus.setSelected( false );
            }
            else
            m_currentState = NONE;
        }    
    }

    public void mouseReleased(MouseEvent e) {}

    public void mouseClicked(MouseEvent e) {}

    public void mouseEntered(MouseEvent e) {}

    public void mouseExited(MouseEvent e) {}
}
