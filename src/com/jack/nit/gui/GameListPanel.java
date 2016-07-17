package com.jack.nit.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

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

@SuppressWarnings("serial")
public class GameListPanel extends JPanel
{
  private final JTree tree;
  private final DefaultTreeModel model;
  private final JScrollPane pane;
  private final SearchPanel search;
  private final JLabel status;
  
  private final JButton collapseAll, expandAll;
  
  private final JCheckBox flatten;
  
  private final TitleNormalizer normalizer;
  private final BiasSet biasSet;
  
  private GameSet set;
  private Predicate<Game> filter;
  
  private int gamesCount;
  private int singleCount;
  private int clonesCount;
  
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
          Icon icon = Location.iconForLocation(location);
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
        label.setText(normalizer.normalize(clone.getTitleForBias(biasSet))+" ("+clone.stream().filter(g -> filter.test(g)).count()+" clones)");
        label.setForeground(Color.DARK_GRAY);
        
        long mask = clone.stream().map(g -> g.info().location).filter(LocationSet::isLocalized).reduce(0L, (m,l) -> m | l.getMask(), (u,v) -> u | v);
        Location location = Location.locationForMask(mask);
        
        if (location != null)
        {
          Icon icon = Location.iconForLocation(new LocationSet(location));
          label.setIcon(icon != null ? icon.getIcon() : null);
        }
        
        
      }
      
      return label;
    }
  }
  
  GameListPanel()
  {
    search = new SearchPanel(() -> populate(set));
    status = new JLabel();
    status.setHorizontalAlignment(SwingConstants.RIGHT);
    
    collapseAll = new JButton("collapse");
    expandAll = new JButton("expand");
    
    flatten = new JCheckBox("flatten");
    
    JPanel upper = new JPanel();
    upper.add(collapseAll);
    upper.add(expandAll);
    upper.add(flatten);
    upper.add(status);
        
    tree = new JTree();
    pane = new JScrollPane(tree);
    model = (DefaultTreeModel)tree.getModel();
    
    tree.setRootVisible(false);
    tree.setCellRenderer(new MyTreeCellRenderer());
    
    pane.setPreferredSize(new Dimension(800,800));
    
    normalizer = new TitleNormalizer();
    biasSet = new BiasSet(Zone.ITALY, Zone.EUROPE, Zone.USA, Zone.JAPAN);
    
    model.setRoot(null);
    
    collapseAll.addActionListener(e -> {
      for (int i = 0; i < tree.getRowCount(); ++i)
        tree.collapseRow(i);
    });
    
    expandAll.addActionListener(e -> {
      for (int i = 0; i < tree.getRowCount(); ++i)
        tree.expandRow(i);
    });
    
    flatten.addActionListener(e -> populate(set));

    setLayout(new BorderLayout());
    add(pane, BorderLayout.CENTER);
    add(search, BorderLayout.SOUTH);
    add(upper, BorderLayout.NORTH);
  }
  
  void populate(GameSet set)
  {
    if (set != null)
    {
    gamesCount = clonesCount = singleCount = 0;
    
    this.set = set;
    this.filter = search.buildSearchPredicate();
    
    if (set.clones() != null && !flatten.isSelected())
      populateByClones();
    else
      populateBySingleGame();
    
    status.setText(gamesCount+" games ("+singleCount+" singles, "+clonesCount+" clones)");
    }
    else
      status.setText("");
  }
  
  private DefaultMutableTreeNode createNodeForGame(Game game, boolean normalizedName)
  {
    DefaultMutableTreeNode gameNode = new GameNode(game, normalizedName);
    
    for (Rom rom : game)
    {
      DefaultMutableTreeNode romNode = new RomNode(rom);
      gameNode.add(romNode);
    }
    
    ++gamesCount;
    return gameNode;
  }
  
  private DefaultMutableTreeNode createNodeForClone(GameClone clone)
  {
    if (clone.size() > 1)
    {
      DefaultMutableTreeNode gameNode = new CloneNode(clone);
      clone.stream()
        .filter(filter)
        .map(g -> createNodeForGame(g, false))
        .forEach(gameNode::add);
      
      ++clonesCount;
      return gameNode;
    }
    else
    {
      ++singleCount;
      return createNodeForGame(clone.get(0), true);
    }
  }
  
  void populateByClones()
  {
    List<DefaultMutableTreeNode> clones = set.clones().stream()
        .filter(c -> c.size() > 0 && c.stream().anyMatch(filter))
        .map(this::createNodeForClone)
        .collect(Collectors.toList());
    
    clones.addAll(set.stream()
        .filter(game -> set.clones().get(game) == null && filter.test(game))
        .map(g -> createNodeForGame(g, false))
        .collect(Collectors.toList())
    );
    
    //clones.sort((n1, n2) -> ((String)n1.getUserObject()).compareToIgnoreCase((String)n2.getUserObject()));
    
    DefaultMutableTreeNode root = new DefaultMutableTreeNode();
    
    clones.forEach(root::add);
    
    model.setRoot(root);
  }
  
  private void populateBySingleGame()
  {
    DefaultMutableTreeNode root = new DefaultMutableTreeNode();
    
    set.stream().filter(filter).map(g -> createNodeForGame(g, false)).forEach(root::add);
   
    model.setRoot(root);
  }
  
}
