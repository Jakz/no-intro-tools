package com.github.jakz.nit.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.List;
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

import com.github.jakz.nit.data.Game;
import com.github.jakz.nit.data.GameSet;
import com.github.jakz.nit.data.xmdb.GameClone;
import com.github.jakz.nit.merger.TitleNormalizer;
import com.github.jakz.romlib.data.game.BiasSet;
import com.github.jakz.romlib.data.game.Location;
import com.github.jakz.romlib.data.game.LocationSet;
import com.github.jakz.romlib.data.game.Rom;
import com.github.jakz.romlib.ui.Icon;
import com.pixbits.lib.lang.StringUtils;

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
        
        LocationSet location = game.info().getLocation();
        if (location.isLocalized())
        {            
          Icon icon = location.getIcon();
          label.setIcon(icon != null ? icon.getIcon() : null);
        }
      }
      else if (value instanceof RomNode)
      {
        Rom rom = (Rom)((RomNode)value).getUserObject();
        label.setText(rom.name+" ["+StringUtils.humanReadableByteCount(rom.size.bytes())+"]");
        label.setIcon(Icon.ROM.getIcon());
      }
      else if (value instanceof CloneNode)
      {
        GameClone clone = (GameClone)((CloneNode)value).getUserObject();
        label.setText(normalizer.normalize(clone.getBestMatchForBias(biasSet, true).name)+" ("+clone.stream().filter(g -> filter.test(g)).count()+" clones)");
        label.setForeground(Color.DARK_GRAY);
        
        long mask = clone.stream().map(g -> g.info().getLocation()).filter(LocationSet::isLocalized).reduce(0L, (m,l) -> m | l.getMask(), (u,v) -> u | v);
        LocationSet set = new LocationSet(mask);
        Location location = set.getExactLocation();

        Icon icon = location.icon;
        label.setIcon(icon != null ? icon.getIcon() : null);
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
    biasSet = new BiasSet(Location.ITALY, Location.EUROPE, Location.USA, Location.JAPAN);
    
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
    {
      status.setText("");
      tree.setModel(new DefaultTreeModel(null));
    }
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
