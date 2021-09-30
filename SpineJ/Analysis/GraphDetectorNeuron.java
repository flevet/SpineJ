/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package SpineJ.Analysis;

import SpineJ.Tools.Graph.Edge;
import SpineJ.Tools.Graph.EdgeGraph;
import SpineJ.Tools.Graph.EdgeWeightedGraph;
import SpineJ.Tools.Graph.SeedGraph;
import SpineJ.Tools.Delaunay.DelaunayT;
import SpineJ.Tools.Delaunay.EdgeT;
import SpineJ.Tools.Delaunay.SeedT;
import ij.measure.Calibration;
import ij.process.ByteProcessor;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 *
 * @author Florian Levet
 */
public class GraphDetectorNeuron{
    protected ArrayList < EdgeWeightedGraph > m_graphs = null;
    protected ByteProcessor m_binary = null;

    public GraphDetectorNeuron(){

    }

    public void execute( ByteProcessor _bp, DelaunayT[] _delaunays ){
        m_binary = _bp;
        
        m_graphs = new ArrayList < EdgeWeightedGraph >();
        for( int n = 0; n < _delaunays.length; n++ ){
            ArrayList < SeedGraph > seeds = _delaunays[n].m_seedsSkel;
            ArrayList < EdgeGraph > edges = _delaunays[n].m_edgesSkel;
            if(!seeds.isEmpty() && !edges.isEmpty()){
                EdgeWeightedGraph graph = new EdgeWeightedGraph( seeds.size() );
                graph.setSeeds( seeds );
                try{
                   for( int i = 0; i < edges.size(); i++ ){
                        EdgeGraph e = edges.get( i );
                        graph.addEdge( new Edge( e.getV1().getId(), e.getV2().getId(), 1, e) );
                    }

                    m_graphs.add( graph );
                }
                catch(Exception e){
                    System.out.println("Problem");
                }
            }
        }
        Collections.sort( m_graphs, new Comparator(){
            public int compare( Object o1, Object o2 ){
                return ( ( EdgeWeightedGraph ) o2 ).E() - ( ( EdgeWeightedGraph ) o1 ).E();
            }
        });
        
        resetSegmentation();
    }
    
    //Merge coarser graph representation with the one used by the remaining algorithm
    //Done in order to eliminate spurious small graph edges, as well as transitional edges
    public void execute(ByteProcessor _bp, DelaunayT[] _delaunays, DelaunayT[] _delaunaysCoarser){
        m_binary = _bp;
        m_graphs = new ArrayList < EdgeWeightedGraph >();
        
        for( int n = 0; n < _delaunays.length; n++ ){
            ArrayList < SeedGraph > seedsNormal = _delaunays[n].m_seedsSkel;
            ArrayList < EdgeGraph > edgesNormal = _delaunays[n].m_edgesSkel;
            ArrayList < SeedGraph > seedsCoarser = _delaunaysCoarser[n].m_seedsSkel;
            ArrayList < EdgeGraph > edgesCoarser = _delaunaysCoarser[n].m_edgesSkel;
            
            if(seedsNormal.isEmpty() || edgesNormal.isEmpty() ||
                    seedsCoarser.isEmpty() || edgesCoarser.isEmpty()) continue;
            
            for(SeedGraph s : seedsNormal)
                s.setMarked(false);
            for(EdgeGraph s : edgesNormal)
                s.setMarked(false);
            for(SeedGraph s : seedsCoarser)
                s.setMarked(false);
            for(EdgeGraph s : edgesCoarser)
                s.setMarked(false);
            
            //First we link every seed of the normal graph rep to the closest one of the coarser rep
            //But each coarser seed is linking to only one normal seed
            SeedGraph closestSeedsToNormal[] = new SeedGraph[seedsNormal.size()], closestSeedsToCoarser[] = new SeedGraph[seedsCoarser.size()];
            int cpt = 0;
            for(SeedGraph s : seedsCoarser){
                double dMin = Double.MAX_VALUE, d;
                for(SeedGraph s2 : seedsNormal){
                    d = s.getPosition().distance(s2.getPosition());
                    if(d < dMin){
                        dMin = d;
                        closestSeedsToCoarser[cpt] = s2;
                    }
                }
                cpt++;
            }
            SeedGraph sTmp = null;
            cpt = 0;
            for(SeedGraph s : seedsNormal){
                closestSeedsToNormal[cpt] = null;
                double dMin = Double.MAX_VALUE, d;
                for(SeedGraph s2 : seedsCoarser){
                    d = s.getPosition().distance(s2.getPosition());
                    if(d < dMin){
                        dMin = d;
                        sTmp = s2;
                    }
                }
                int index = seedsCoarser.indexOf(sTmp);
                if(s == closestSeedsToCoarser[index])
                    closestSeedsToNormal[cpt] = sTmp;
                cpt++;
            }
            
            //We start from an extream seeds
            cpt = 0;
            SeedGraph currentSeed = null;
            while(seedsNormal.get(cpt).degree() != 1) currentSeed = seedsNormal.get(cpt++);
            if(currentSeed == null) continue;
            
            ArrayList < SeedGraph > mergedSeeds = new ArrayList < SeedGraph >();
            ArrayList < EdgeT > mergedEdges = new ArrayList < EdgeT >();
            ArrayList < SeedT > seedsTmp  = new ArrayList < SeedT >();
            ArrayList < EdgeT > edgesTmp  = new ArrayList < EdgeT >();
            cpt = 0;
            for(SeedGraph s : seedsNormal){
                if(s.degree() != 2)
                    mergedSeeds.add(s);
            }
            EdgeWeightedGraph graph = new EdgeWeightedGraph( seedsNormal.size());//mergedSeeds.size() );
            graph.setSeeds( seedsNormal);//mergedSeeds );
            try{
                for(SeedGraph s : seedsNormal){
                    if(s.degree() <= 2/* || closestSeedsToNormal[cpt++] == null*/) continue;
                    //System.out.println("Seed " + cpt);
                    SeedT seed = (SeedT)s, endingSeed = null;
                    ArrayList <EdgeT> edges = seed.getEdges();
                    for(EdgeT e : edges){
                        if(e.marked()) continue;
                        EdgeT currentEdge = e;
                        edgesTmp.clear();
                        seedsTmp.clear();
                        edgesTmp.add(currentEdge);
                        currentEdge.setMarked(true);
                        SeedT otherS = currentEdge.getOtherSeed(seed);
                        while(otherS != null){
                            if(otherS.degree() == 1){
                                sTmp = closestSeedsToNormal[seedsNormal.indexOf(otherS)];
                                if(sTmp == null)
                                    endingSeed = null;
                                else
                                    endingSeed = otherS;
                                otherS = null;
                            }
                            else if(otherS.degree() == 2){
                                currentEdge = otherS.getOtherEdge(currentEdge);
                                edgesTmp.add(currentEdge);
                                currentEdge.setMarked(true);
                                seedsTmp.add(otherS);
                                otherS = currentEdge.getOtherSeed(otherS);
                            }
                            else{
                                endingSeed = otherS;
                                otherS = null;
                            }
                        }
                        //if endingSeed is null, we have a skeletonBranch that exists in the normal rep
                        //but doesn't in the coarser rep -> every triangles is added to the original seed
                        if(endingSeed == null){
                            for(SeedT st : seedsTmp){
                                seed.addTrianglesTrimmed(st.getTriangleTrimmed());
                                seed.addTriangleTrimmed(st.getTriangle());
                            }
                            for(EdgeT et : edgesTmp){
                                seed.addTrianglesTrimmed(et.getTriangles());
                                seed.addTrianglesTrimmed(et.getTrianglesTrimmed());
                            }
                        }
                        else{
                            EdgeT newEdge = new EdgeT(seed, endingSeed);
                            for(SeedT st : seedsTmp){
                                newEdge.addTrianglesTrimmed(st.getTriangleTrimmed());
                                newEdge.addTriangle(st.getTriangle());
                            }
                            for(EdgeT et : edgesTmp){
                                newEdge.addTriangles(et.getTriangles());
                                newEdge.addTrianglesTrimmed(et.getTrianglesTrimmed());
                            }
                            newEdge.recomputeSkeleton();
                            mergedEdges.add(newEdge);
                            graph.addEdge( new Edge( newEdge.getV1().getId(), newEdge.getV2().getId(), 1, newEdge) );
                        }
                    }
                }
            }
            catch(Exception e){
                System.out.println("Problem");
            }
            for(SeedGraph sg : mergedSeeds){
                SeedT st = (SeedT)sg;
                st.resetEdges();
            }
            for(EdgeT et : mergedEdges){
                et.getV1().addEdge(et);
                et.getV2().addEdge(et);
            }
            m_graphs.add( graph );
        }
         Collections.sort( m_graphs, new Comparator(){
            public int compare( Object o1, Object o2 ){
                return ( ( EdgeWeightedGraph ) o2 ).E() - ( ( EdgeWeightedGraph ) o1 ).E();
            }
        });
        
        resetSegmentation();
    }

   public ArrayList < EdgeWeightedGraph > getGraphs(){
        return m_graphs;
    }

    public void resetSegmentation(){
        for( int i = 0; i < m_graphs.size(); i++ ){
            EdgeWeightedGraph g = m_graphs.get( i );
            for( Edge e : g.edges() ){
                EdgeT edge = ( EdgeT )( e.edge() );
                edge.setAsDendrite( false );
                edge.getV1().getTriangle().setMarked( false );
                edge.getV2().getTriangle().setMarked( false );
            }
        }
    }

    public void setAllAsDendrite(){
        for( int i = 0; i < m_graphs.size(); i++ ){
            EdgeWeightedGraph g = m_graphs.get( i );
            for( Edge e : g.edges() ){
                EdgeT edge = ( EdgeT )( e.edge() );
                edge.setAsDendrite( true );
                edge.getV1().getTriangle().setMarked( true );
                edge.getV2().getTriangle().setMarked( true );
            }
        }
    }
    
    //Test if the seed, which is junctional (in our case degree must be 3) is probably in a spine
    //If two of the three seeds connected to s have trimmed triangles, we suspect it is in a spine
    public boolean isJunctionnalSeedInSpine(SeedT s){
        if(s.degree() != 3) return false;
        int nbSeedWithTrimmedTriangles = 0;
        ArrayList < EdgeT > edges = s.getEdges();
        //System.out.println("******************************");
        //System.out.println("For seed x = " + s.x + ", y = " + s.y + ", nb edges = " + edges.size());
        for(EdgeT e : edges){
            SeedT other = e.getOtherSeed(s);
            if(/*other.hasTrimmedTriangles()*/other.degree() == 1) nbSeedWithTrimmedTriangles++;
            //System.out.println("Connected vertex is x = " + other.x + ", y = " + other.y + ", degree = " + other.degree() + ", has trimmed ? " + other.hasTrimmedTriangles());
        }
        return nbSeedWithTrimmedTriangles >= 2;
    }
    
    public ArrayList < ArrayList < EdgeGraph > > getAllSpinesAsGraph2(){
        ArrayList < ArrayList < EdgeGraph > > spines = new ArrayList < ArrayList < EdgeGraph > >();
        for( int i = 0; i < m_graphs.size(); i++ ){
            //Set all as dendrite
            EdgeWeightedGraph g = m_graphs.get( i );
            for( Edge e : g.edges() ){
                EdgeT edge = ( EdgeT )( e.edge() );
                edge.setAsDendrite( true );
                edge.setMarked(false);
                edge.getV1().getTriangle().setMarked( true );
                edge.getV2().getTriangle().setMarked( true );
            }
            
            /*ArrayList < EdgeGraph > allEdges = new ArrayList < EdgeGraph >();
            int currentEdge = 0;
            ArrayList < SeedGraph > seeds = g.getSeeds();
            for(SeedGraph sg : seeds){
                if(sg.degree() == 1){
                    SeedT seed = (SeedT)sg;
                    seed.setMarked(false);
                    EdgeT e = seed.getEdges().get(0);
                    allEdges.add(e);
                    while(currentEdge < allEdges.size()){
                        e = (EdgeT)allEdges.get(currentEdge++);
                        e.setMarked(true);
                        e.setAsDendrite(false);
                        SeedT[] seedsTmp = {e.getV1(), e.getV2()};
                        for(int n = 0; n < 2; n++){
                            SeedT sdt = seedsTmp[n];
                            if(sdt.isMarked()){
                                if(sdt.degree() == 1)
                                    sdt.setMarked(false);
                                else if(sdt.degree() == 2){
                                    e = sdt.getOtherEdge(e);
                                    if(!e.marked())
                                        allEdges.add(e);
                                    sdt.setMarked(false);
                                }
                            }
                        }
                    }
                }
            }
            
            //Test if there are junctional triangles in the spine
            //Two of their three edges are marked
            for(SeedGraph sg : seeds){
                if(sg.degree() == 3){
                    SeedT seed = (SeedT)sg;
                    ArrayList < EdgeT > edges = seed.getEdges();
                    int nbMarkedEdges = 0;
                    for(EdgeT e : edges)
                        if(e.marked())
                            nbMarkedEdges++;
                    if(nbMarkedEdges >= 2)
                        for(EdgeT e : edges){
                            if(!e.marked()){
                                e.setMarked(true);
                                e.setAsDendrite(false);
                                allEdges.add(e);
                                System.out.println("Adding edge " + e.toString());
                            }
                        }
                }
            }
            
            for(EdgeGraph eg : allEdges){
                if(eg.marked()){
                    ArrayList < EdgeGraph > currentSpine = new ArrayList < EdgeGraph >();
                    EdgeT e = (EdgeT)eg;
                    currentSpine.add(e);
                    currentEdge = 0;
                    while(currentEdge < currentSpine.size()){
                        e = (EdgeT)currentSpine.get(currentEdge++);
                        e.setMarked(false);
                        for(EdgeGraph eg2 : e.getV1().getEdges())
                            if(eg2.marked())
                                currentSpine.add(eg2);
                        for(EdgeGraph eg2 : e.getV2().getEdges())
                            if(eg2.marked())
                                currentSpine.add(eg2);
                    }
                    spines.add(currentSpine);
                }
                
            }*/
            
            //Get connected components of all the edges selected
            
            //Determine an extremal graph point
            ArrayList < SeedGraph > seeds = g.getSeeds();
            for(SeedGraph sg : seeds){
                if(sg.degree() == 1 && sg.isMarked()){
                    SeedT stoppingVertex = null;
                    int currentEdge = 0;
                    ArrayList < EdgeGraph > currentSpine = new ArrayList < EdgeGraph >();
                    SeedT seed = (SeedT)sg;
                    seed.setMarked(false);
                    EdgeT e = seed.getEdges().get(0);
                    e.setMarked(true);
                    e.setAsDendrite(false);
                    currentSpine.add(e);
                    while(currentEdge < currentSpine.size()){
                        e = (EdgeT)currentSpine.get(currentEdge++);
                        e.setMarked(true);
                        e.setAsDendrite(false);
                        SeedT[] seedsTmp = {e.getV1(), e.getV2()};
                        for(int n = 0; n < 2; n++){
                            SeedT sdt = seedsTmp[n];
                            if(sdt.isMarked()){
                                if(sdt.degree() == 1)
                                    sdt.setMarked(false);
                                else if(sdt.degree() == 2){
                                    e = sdt.getOtherEdge(e);
                                    if(!e.marked())
                                        currentSpine.add(e);
                                    sdt.setMarked(false);
                                }
                                else if(isJunctionnalSeedInSpine(sdt)){
                                    for(EdgeT eTmp : sdt.getEdges()){
                                        if(!eTmp.marked())
                                            currentSpine.add(eTmp);
                                    }
                                    sdt.setMarked(false);
                                }
                                else
                                    stoppingVertex = sdt;
                            }
                        }
                    }
                    //if(stoppingVertex != null)
                    //    for(EdgeT ett : stoppingVertex.getEdges())
                    //        ett.setAsDendrite(true);
                    spines.add(currentSpine);
                    //return spines;
                }
            }

            
        }
        //setAllAsDendrite();
        return spines;
    }
    
    public ArrayList < ArrayList < EdgeGraph > > getAllSpinesAsGraph(int _w, int _h, Calibration _cal, double _d){
        ArrayList < ArrayList < EdgeGraph > > spines = new ArrayList <  >();
        for (int n = 0; n < m_graphs.size(); n++){
            ArrayList < ArrayList < EdgeGraph > > tmp = getAllSpinesFromOneGraph(n, _w, _h, _cal, _d);
            spines.addAll(tmp);
        }
        return spines;
    }
    
    public ArrayList < ArrayList < EdgeGraph > > getAllSpinesFromOneGraph(int _index, int _w, int _h, Calibration _cal, double _d){
        double distToMerge = _cal.getRawX(_d);
        ArrayList < ArrayList < EdgeGraph > > spines = new ArrayList <  >();
        EdgeWeightedGraph g = m_graphs.get(_index);
        for( Edge e : g.edges() ){
            EdgeT edge = ( EdgeT )( e.edge() );
            edge.setAsDendrite( true );
            edge.setMarked(false);
            edge.getV1().getTriangle().setMarked( true );
            edge.getV2().getTriangle().setMarked( true );
        }

        //If a seed is extremal, it is not marked, if it is junctional, it is marked
        for(SeedGraph sg : g.getSeeds()){
            sg.setMarked(sg.degree() != 1 || ((SeedT)sg).isTouchingBorderImage(_w, _h));
        }

        ArrayList <SeedGraph> sgs = g.getSeeds();
        boolean modified = false;
        do{
            modified = false;
            for(int n = 0; n < sgs.size(); n++){
                SeedGraph sg = sgs.get(n);
                if(!sg.isMarked()) continue;
                SeedT seed = (SeedT)sg;
                int nbJunctionalNeighbors = 3;
                for(EdgeT e : seed.getEdges()){
                    SeedT o = e.getOtherSeed(seed);
                    if(!o.isMarked() && Point2D.Double.distance(seed.x, seed.y, o.x, o.y) < distToMerge)
                        nbJunctionalNeighbors--;
                }
                if(nbJunctionalNeighbors < 2){
                    //Test if the seed is touching the border of the image, in this case it sticks to being a junctional node, no matter what
                    if(!seed.isTouchingBorderImage(_w, _h)){
                        seed.setMarked(false);
                        modified = true;
                    }
                }
            }
        }while(modified);

        //Now we will merge every edge of seeds that are neighbors and marked
        for(int n = 0; n < sgs.size(); n++){
            SeedGraph sg = sgs.get(n);
            if(!sg.isMarked()){
                ArrayList < EdgeGraph > currentSpine = new ArrayList < EdgeGraph >();
                SeedT seed = (SeedT)sg;
                for(EdgeT eTmp : seed.getEdges())
                    if(!eTmp.marked()){
                        currentSpine.add(eTmp);
                        eTmp.setMarked(true);
                        eTmp.setAsDendrite(false);
                    }

                int currentEdge = 0;
                while(currentEdge < currentSpine.size()){
                    EdgeT e = (EdgeT)currentSpine.get(currentEdge++);
                    SeedT[] seedsTmp = {e.getV1(), e.getV2()};
                    for(int i = 0; i < 2; i++){
                        SeedT sdt = seedsTmp[i];
                        if(!sdt.isMarked()){
                            for(EdgeT eTmp : sdt.getEdges()){
                                if(!eTmp.marked()){
                                    SeedT o = eTmp.getOtherSeed(sdt);
                                    if(!sdt.isMarked() || !o.isMarked()){
                                        currentSpine.add(eTmp);
                                        eTmp.setMarked(true);
                                        eTmp.setAsDendrite(false);
                                    }
                                }
                            }
                        }
                    }
                }
                if(!currentSpine.isEmpty())
                    spines.add(currentSpine);
            }
        }
        return spines;
    }
}
