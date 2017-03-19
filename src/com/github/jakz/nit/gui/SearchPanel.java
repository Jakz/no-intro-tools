package com.github.jakz.nit.gui;

import java.awt.Color;
import java.awt.Component;
import java.util.Arrays;
import java.util.function.Predicate;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import com.github.jakz.nit.data.Game;
import com.github.jakz.nit.data.Location;
import com.github.jakz.nit.data.Searcher;
import com.github.jakz.romlib.ui.Icon;
import com.pixbits.lib.ui.elements.JPlaceHolderTextField;

public class SearchPanel extends JPanel
{
  private static final long serialVersionUID = 1L;
  
  private Runnable callback;
  private final Searcher searcher;
  
  final private JLabel[] labels = new JLabel[3];
  final private JPlaceHolderTextField freeSearchField = new JPlaceHolderTextField(10, "free search");
  
  //final JComboBox genres = new JComboBox();
  final private JComboBox<Location> locations = new JComboBox<>();
  final private JComboBox<Location> languages = new JComboBox<>();
      
  boolean active = false;

  abstract class CustomCellRenderer<T> implements ListCellRenderer<T>
  {
    private javax.swing.plaf.basic.BasicComboBoxRenderer renderer;
    
    CustomCellRenderer()
    {
      renderer = new javax.swing.plaf.basic.BasicComboBoxRenderer();
    }
    
    @Override
    public Component getListCellRendererComponent(JList<? extends T> list, T obj, int index, boolean isSelected, boolean cellHasFocus)
    {
      JLabel label = (JLabel)renderer.getListCellRendererComponent(list, obj, index, isSelected, cellHasFocus);

      customRendering(label, obj);
      return label;
    }
    
    abstract void customRendering(JLabel label, T value);
  }
  
  class LocationCellRenderer extends CustomCellRenderer<Location>
  {
    @Override
    void customRendering(JLabel label, Location location)
    {
      if (location == null)
      {
        label.setText("Location");
        label.setIcon(null);
      }
      else
      {
        label.setText(location.name);
        Icon icon = Location.iconForLocation(location);
        label.setIcon(icon != null ? icon.getIcon() : null);
      }
    }
  }
  
  class LanguageCellRenderer extends CustomCellRenderer<Location>
  {
    @Override
    void customRendering(JLabel label, Location language)
    {
      if (language == null)
      {
        label.setText("Language");
        label.setIcon(null);
      }
      else
      {
        label.setText(language.language);
        Icon icon = Location.iconForLocation(language);
        label.setIcon(icon != null ? icon.getIcon() : null);
      }
    }
  }

  void activate(boolean active)
  {
    this.active = active;
  }
  
  void toggle(boolean enabled)
  {
    freeSearchField.setEnabled(enabled);
    
    if (!enabled)
      freeSearchField.setText("");
    
    freeSearchField.setPlaceholder("free search");
  }
  
  private void setComboBoxSelectedBackground(JComboBox<?> comboBox)
  {
    Object child = comboBox.getAccessibleContext().getAccessibleChild(0);
    javax.swing.plaf.basic.BasicComboPopup popup = (javax.swing.plaf.basic.BasicComboPopup)child;
    JList<?> list = popup.getList();
    list.setSelectionBackground(new Color(164,171,184)); //TODO: hacked
  }
    
  public SearchPanel(Runnable callback)
  {    
    this.callback = callback;
    this.searcher = new Searcher();
    
    labels[0] = new JLabel();
    labels[1] = new JLabel();
    labels[2] = new JLabel();
    

    setComboBoxSelectedBackground(locations);
    setComboBoxSelectedBackground(languages);
    
    locations.addItem(null);
    Arrays.stream(Location.values())
      .filter(l -> !l.isComposite())
      .sorted((l1,l2) -> l1.name.compareToIgnoreCase(l2.name))
      .forEach(locations::addItem);
    
    locations.setRenderer(new LocationCellRenderer());
    
    languages.addItem(null);
    Arrays.stream(Location.values()).filter(l -> !l.isComposite()).forEach(languages::addItem);
    languages.setRenderer(new LanguageCellRenderer());
    
    freeSearchField.addCaretListener(e -> { if (active) invokeRefresh(); });
    locations.addActionListener(e -> { if (active) invokeRefresh(); });
    languages.addActionListener(e -> { if (active) invokeRefresh(); });

    this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
    
    this.add(labels[0]);
    this.add(freeSearchField);
    this.add(labels[1]);
    this.add(locations);
    this.add(labels[2]);
    this.add(languages);
    
    active = true;
  }
  
  public Predicate<Game> buildSearchPredicate()
  {
    Predicate<Game> filter = searcher.buildPredicate(freeSearchField.getText());
    
    Location location = locations.getItemAt(locations.getSelectedIndex());
    
    if (location != null)
      filter = filter.and(g -> g.info().location.isJust(location));
      
    Location language = languages.getItemAt(languages.getSelectedIndex());
    
    if (language != null)
      filter = filter.and(g -> g.info().languages.is(language) || g.info().location.isJust(language));
    
    return filter;
  }
  
  private void invokeRefresh()
  {
    callback.run(); 
  }
}