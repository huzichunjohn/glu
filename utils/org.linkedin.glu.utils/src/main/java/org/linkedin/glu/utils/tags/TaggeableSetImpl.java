/*
 * Copyright (c) 2011 Yan Pujante
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.linkedin.glu.utils.tags;

import java.util.Collection;
import java.util.Set;

/**
 * @author yan@pongasoft.com
 */
public abstract class TaggeableSetImpl implements Taggeable
{
  /**
   * YP implementation note: for fast access, this set will never be synchronized as
   * it will never be modified! It will be replaced when it changes, hence the volatile keyword
   */
  private volatile Set<String> _tags;

  /**
   * Constructor
   */
  public TaggeableSetImpl()
  {
    _tags = createEmptySet();
  }

  protected abstract Set<String> createEmptySet();
  protected abstract Set<String> createSet(Collection<String> tags);

  @Override
  public int getTagsCount()
  {
    return _tags.size();
  }

  @Override
  public boolean hasTags()
  {
    return !_tags.isEmpty();
  }

  /**
   * Constructor
   */
  public TaggeableSetImpl(Collection<String> tags)
  {
    _tags = createSet(tags);
  }

  @Override
  public Set<String> getTags()
  {
    return _tags;
  }

  @Override
  public boolean hasTag(String tag)
  {
    return _tags.contains(tag);
  }

  @Override
  public boolean hasAllTags(Collection<String> tags)
  {
    return _tags.containsAll(tags);
  }

  @Override
  public boolean hasAnyTag(Collection<String> tags)
  {
    Set<String> localTags = _tags;

    for(String tag : tags)
    {
      if(localTags.contains(tag))
        return true;
    }

    return false;
  }

  @Override
  public Set<String> getCommonTags(Collection<String> tags)
  {
    Set<String> localTags = _tags;

    Set<String> res = createEmptySet();

    for(String tag : tags)
    {
      if(localTags.contains(tag))
        res.add(tag);
    }

    return res;
  }

  @Override
  public Set<String> getMissingTags(Collection<String> tags)
  {
    Set<String> localTags = _tags;

    Set<String> res = createEmptySet();

    for(String tag : tags)
    {
      if(!localTags.contains(tag))
        res.add(tag);
    }

    return res;
  }

  @Override
  public boolean addTag(String tag)
  {
    synchronized(this)
    {
      if(_tags.contains(tag))
        return false;

      Set<String> newTags = createSet(_tags);
      newTags.add(tag);
      _tags = newTags;

      return true;
    }
  }

  @Override
  public Set<String> addTags(Collection<String> tags)
  {
    Set<String> res = createEmptySet();

    synchronized(this)
    {
      Set<String> newTags = createSet(_tags);

      for(String tag : tags)
      {
        if(!newTags.add(tag))
          res.add(tag);
      }

      _tags = newTags;
    }

    return res;
  }

  @Override
  public boolean removeTag(String tag)
  {
    synchronized(this)
    {
      if(!_tags.contains(tag))
        return false;

      Set<String> newTags = createSet(_tags);
      newTags.remove(tag);
      _tags = newTags;

      return true;
    }
  }

  @Override
  public Set<String> removeTags(Collection<String> tags)
  {
    Set<String> res = createEmptySet();

    synchronized(this)
    {
      Set<String> newTags = createSet(_tags);

      for(String tag : tags)
      {
        if(!newTags.remove(tag))
          res.add(tag);
      }

      _tags = newTags;
    }

    return res;
  }

  @Override
  public void setTags(Collection<String> tags)
  {
    synchronized(this)
    {
      _tags = createSet(tags);
    }
  }

  @Override
  public boolean equals(Object o)
  {
    if(this == o) return true;
    if(!(o instanceof TaggeableSetImpl)) return false;

    TaggeableSetImpl taggeable = (TaggeableSetImpl) o;

    if(!_tags.equals(taggeable._tags)) return false;

    return true;
  }

  @Override
  public int hashCode()
  {
    return _tags.hashCode();
  }
}
