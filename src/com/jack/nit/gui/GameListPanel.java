package com.jack.nit.gui;

import java.awt.Dimension;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

import com.jack.nit.data.Game;
import com.jack.nit.data.GameSet;
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
  
  GameListPanel()
  {
    tree = new JTree();
    pane = new JScrollPane(tree);
    model = (DefaultTreeModel)tree.getModel();
    
    tree.setRootVisible(false);
    
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
    DefaultMutableTreeNode gameNode = new DefaultMutableTreeNode(normalizedName ? normalizer.normalize(game.name) : game.name);
    
    for (Rom rom : game)
    {
      DefaultMutableTreeNode romNode = new DefaultMutableTreeNode(rom.name+" ["+StringUtils.humanReadableByteCount(rom.size)+"]");
      gameNode.add(romNode);
    }
    
    return gameNode;
  }
  
  private DefaultMutableTreeNode createNodeForClone(GameClone clone)
  {
    if (clone.size() > 1)
    {
      DefaultMutableTreeNode gameNode = new DefaultMutableTreeNode(normalizer.normalize(clone.getTitleForBias(biasSet)));
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
    
    clones.sort((n1, n2) -> ((String)n1.getUserObject()).compareToIgnoreCase((String)n2.getUserObject()));
    
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
