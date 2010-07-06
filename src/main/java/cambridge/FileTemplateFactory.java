package cambridge;

import cambridge.model.FragmentList;
import cambridge.model.TemplateDocument;

import java.io.File;
import java.util.HashSet;
import java.util.Locale;

/**
 * User: erdinc
 * Date: Nov 3, 2009
 * Time: 4:03:22 PM
 */

class FileTemplateFactory extends TemplateFactory {
   private final File templateFile;
   private final String encoding;
   private final TemplateModifier modifier;
   private long lastCheck;
   private HashSet<File> includes;

   public static long ChangeDetectionInterval = 5000L;

   public FileTemplateFactory(TemplateLoader loader, FragmentList fragments, File templateFile, String encoding) {
      this(loader, fragments, templateFile, encoding, null, null);
   }

   public FileTemplateFactory(TemplateLoader loader, FragmentList fragments, File templateFile, String encoding, TemplateModifier modifier) {
      this(loader, fragments, templateFile, encoding, modifier, null);
   }

   public FileTemplateFactory(TemplateLoader loader, FragmentList fragments, File templateFile, String encoding, HashSet<File> includes) {
      this(loader, fragments, templateFile, encoding, null, includes);
   }

   public FileTemplateFactory(TemplateLoader loader, FragmentList fragments, File templateFile, String encoding, TemplateModifier modifier, HashSet<File> includes) {
      super(loader, fragments);
      this.templateFile = templateFile;
      this.encoding = encoding;
      this.modifier = modifier;
      this.includes = includes;
      lastCheck = System.currentTimeMillis();
   }

   @Override
   public Template createTemplate() {
      checkForChanges();

      return new DynamicTemplate(fragments);
   }

   @Override
   public Template createTemplate(Locale locale) {
      checkForChanges();

      return new DynamicTemplate(fragments, locale);
   }

   private void checkForChanges() {
      if (ChangeDetectionInterval != -1 && !reloading && lastCheck + ChangeDetectionInterval < System.currentTimeMillis()) {
         if (templateFile.lastModified() > lastCheck) {
            reload();
         } else if (includes != null) {
            for (File f : includes) {
               if (f.exists() && f.lastModified() > lastCheck) {
                  reload();
                  break;
               }
            }
         }

         lastCheck = System.currentTimeMillis();
      }
   }

   private boolean reloading;

   private synchronized void reload() {
      reloading = true;
      FileTemplateLoader l = (FileTemplateLoader) loader;
      try {
         TemplateDocument doc = l.parseTemplate(templateFile, encoding);
         if (modifier != null) {
            modifier.modifyTemplate(doc);
         }

         if (doc.getIncludes() != null) {
            includes = l.getFiles(doc.getIncludes());
         }

         fragments = doc.normalize();

      } catch (TemplateLoadingException e) {
         throw new TemplateReloadingException(e);
      } catch (BehaviorInstantiationException e) {
         e.printStackTrace();
         throw new TemplateReloadingException(e);
      }
      reloading = false;
   }
}
