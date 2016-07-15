package com.jack.nit.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

import com.jack.nit.data.Game;
import com.jack.nit.data.GameSet;
import com.jack.nit.data.Location;
import com.jack.nit.data.LocationSet;
import com.jack.nit.data.Rom;
import com.jack.nit.data.xmdb.BiasSet;
import com.jack.nit.data.xmdb.GameClone;
import com.jack.nit.data.xmdb.Zone;
import com.jack.nit.merger.TitleNormalizer;
import com.pixbits.strings.StringUtils;

public class GameListPanel extends JPanel
{
  private final JTree tree;
  private final DefaultTreeModel model;
  private final JScrollPane pane;
  
  private final TitleNormalizer normalizer;
  private final BiasSet biasSet;
  
  private class GameNode extends DefaultMutableTreeNode
  {
    final boolean normalized;
    GameNode(Game game, boolean normalized) { super(game); this.normalized = normalized; }
  }
  
  private class RomNode extends DefaultMutableTreeNode
  {
    RomNode(Rom rom) { super(rom); }
  }
  
  private class CloneNode extends DefaultMutableTreeNode
  {
    CloneNode(GameClone clone) { super(clone); }
  }
  
  private class MyTreeCellRenderer extends DefaultTreeCellRenderer
  {
    private Icon iconForLocation(LocationSet location)
    {
      if (location.isJust(Location.ITALY))
        return Icon.FLAG_ITALY;
      else if (location.isJust(Location.FRANCE))
        return Icon.FLAG_FRANCE;
      else if (location.isJust(Location.GERMANY))
        return Icon.FLAG_GERMANY;
      else if (location.isJust(Location.SPAIN))
        return Icon.FLAG_SPAIN;
      
      else if (location.isJust(Location.EUROPE))
        return Icon.FLAG_EUROPE;

      else if (location.isJust(Location.USA))
        return Icon.FLAG_USA;
      
      else if (location.isJust(Location.KOREA))
        return Icon.FLAG_KOREA;
      else if (location.isJust(Location.CANADA))
        return Icon.FLAG_CANADA;
      
      else if (location.isJust(Location.JAPAN))
        return Icon.FLAG_JAPAN;
      
      else if (location.isJust(Location.USA_EUROPE))
        return Icon.FLAG_USA_EUROPE;
      
      else if (location.isJust(Location.USA_JAPAN))
        return Icon.FLAG_JAPAN_USA;
      
      else if (location.isJust(Location.WORLD))
        return Icon.FLAG_WORLD;
      
      else return null;
    }
    
    @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus)
    {
      JLabel label = (JLabel)super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
      
      label.setIcon(null);
      
      if (value instanceof GameNode)
      {
        GameNode node = (GameNode)value;
        Game game = (Game)((GameNode)value).getUserObject();
        label.setText(node.normalized ? normalizer.normalize(game.name) : game.name);
        label.setForeground(Color.BLACK);
        
        LocationSet location = game.info().location;
        if (location.isLocalized())
        {            
          Icon icon = iconForLocation(location);
          label.setIcon(icon != null ? icon.getIcon() : null);
        }
      }
      else if (value instanceof RomNode)
      {
        Rom rom = (Rom)((RomNode)value).getUserObject();
        label.setText(rom.name+" ["+StringUtils.humanReadableByteCount(rom.size)+"]");
        label.setIcon(Icon.ROM.getIcon());
      }
      else if (value instanceof CloneNode)
      {
        GameClone clone = (GameClone)((CloneNode)value).getUserObject();
        label.setText(normalizer.normalize(clone.getTitleForBias(biasSet))+" ("+clone.size()+" clones)");
        label.setForeground(Color.DARK_GRAY);
        
        long mask = clone.stream().map(g -> g.info().location).filter(LocationSet::isLocalized).reduce(0L, (m,l) -> m | l.getMask(), (u,v) -> u | v);
        Location location = Location.locationForMask(mask);
        
        if (location != null)
        {
          Icon icon = iconForLocation(new LocationSet(location));
          label.setIcon(icon != null ? icon.getIcon() : null);
        }
        
        
      }
      
      return label;
    }
  }
  
  GameListPanel()
  {
    tree = new JTree();
    pane = new JScrollPane(tree);
    model = (DefaultTreeModel)tree.getModel();
    
    tree.setRootVisible(false);
    tree.setCellRenderer(new MyTreeCellRenderer());
    
    pane.setPreferredSize(new Dimension(800,800));
    
    normalizer = new TitleNormalizer();
    biasSet = new BiasSet(Zone.ITALY, Zone.EUROPE, Zone.USA, Zone.JAPAN);
    
    model.setRoot(null);
    
    add(pane);
  }
  
  void populate(GameSet set)
  {
    if (set.clones() != null)
      populateByClones(set);
    else
      populateBySingleGame(set);
  }
  
  private DefaultMutableTreeNode createNodeForGame(Game game, boolean normalizedName)
  {
    DefaultMutableTreeNode gameNode = new GameNode(game, normalizedName);
    
    for (Rom rom : game)
    {
      DefaultMutableTreeNode romNode = new RomNode(rom);
      gameNode.add(romNode);
    }
    
    return gameNode;
  }
  
  private DefaultMutableTreeNode createNodeForClone(GameClone clone)
  {
    if (clone.size() > 1)
    {
      DefaultMutableTreeNode gameNode = new CloneNode(clone); // new DefaultMutableTreeNode(normalizer.normalize(clone.getTitleForBias(biasSet)));
      clone.stream().map(g -> createNodeForGame(g, false)).forEach(gameNode::add);
      return gameNode;
    }
    else
    {
      return createNodeForGame(clone.get(0), true);
    }
  }
  
  void populateByClones(GameSet set)
  {
    List<DefaultMutableTreeNode> clones = set.clones().stream().filter(c -> c.size() > 0).map(this::createNodeForClone).collect(Collectors.toList());
    clones.addAll(set.stream().filter(game -> set.clones().get(game) == null).map(g -> createNodeForGame(g, false)).collect(Collectors.toList()));
    
    //clones.sort((n1, n2) -> ((String)n1.getUserObject()).compareToIgnoreCase((String)n2.getUserObject()));
    
    DefaultMutableTreeNode root = new DefaultMutableTreeNode();
    
    clones.forEach(root::add);
    
    model.setRoot(root);
  }
  
  private void populateBySingleGame(GameSet set)
  {
    DefaultMutableTreeNode root = new DefaultMutableTreeNode();
    
    set.stream().map(g -> createNodeForGame(g, false)).forEach(root::add);
   
    model.setRoot(root);
  }
  
}
