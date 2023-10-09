# Configuration file for the Sphinx documentation builder.
#
# This file only contains a selection of the most common options. For a full
# list see the documentation:
# https://www.sphinx-doc.org/en/master/usage/configuration.html

# -- Path setup --------------------------------------------------------------

# If extensions (or modules to document with autodoc) are in another directory,
# add these directories to sys.path here. If the directory is relative to the
# documentation root, use os.path.abspath to make it absolute, like shown here.
#
# import os
# import sys
# sys.path.insert(0, os.path.abspath('.'))

import os
import re
from glob import glob

# -- Project information -----------------------------------------------------

project = 'Ichthyop'
copyright = '2020, Nicolas Barrier'
author = 'Nicolas Barrier'

# Recover the Ichthyop version based
pom_file = os.path.join('..', 'pom.xml')
with open(pom_file, 'r') as fpom:
    lines = fpom.readlines()
    regex = re.compile(' *\<version\>(.*)\</version\>')
    for l in lines:
        if regex.match(l):
            version = regex.match(l).groups()[0]
            break

# include to to references
todo_include_todos = True
todo_emit_warnings = True

# -- General configuration ---------------------------------------------------

# Add any Sphinx extension module names here, as strings. They can be
# extensions coming with Sphinx (named 'sphinx.ext.*') or your custom
# ones.
extensions = [
    'sphinx.ext.autodoc',
    'sphinx.ext.doctest',
    'sphinx.ext.autosummary',
    'sphinx.ext.todo',
    'sphinx.ext.mathjax',
    'sphinx.ext.intersphinx',
    'sphinx.ext.githubpages',
    'sphinxcontrib.bibtex',
    'sphinxcontrib.programoutput',
    'IPython.sphinxext.ipython_directive',
    'IPython.sphinxext.ipython_console_highlighting',
    'matplotlib.sphinxext.plot_directive',
    'sphinxcontrib.mermaid',
    'myst_parser'
]

plantuml = 'plantuml'
plantuml_output_format = 'svg_img'
plantuml_latex_output_format = 'pdf'

mermaid_pdfcrop = 'pdfcrop'
#mermaid_output_format = 'png'

autodoc_default_flags = ['members', 'undoc-members', 'private-members']
autodoc_default_flags = ['members']
autosummary_generate = True
autoclass_content = 'class'

# Add any paths that contain templates here, relative to this directory.
templates_path = ['_templates']

# List of patterns, relative to source directory, that match files and
# directories to ignore when looking for source files.
# This pattern also affects html_static_path and html_extra_path.
exclude_patterns = ['_build', 'Thumbs.db', '.DS_Store']
exclude_patterns += ['**/*.ipynb']
exclude_patterns += glob(os.path.join('developer','_static', 'mermaid', '*md'))
source_suffix = {'.md': 'markdown'}

myst_enable_extensions = [
    "amsmath",
    "attrs_inline",
    "colon_fence",
    "deflist",
    "dollarmath",
    "fieldlist",
    "html_admonition",
    "html_image",
    "linkify",
    "replacements",
    "smartquotes",
    "strikethrough",
    "substitution",
    "tasklist",
]

myst_substitutions = {
  'ich': 'Ichthyop',
  'nc': 'NetCDF',
}

bibtex_bibfiles = ['_static/biblio.bib']


# -- Options for HTML output -------------------------------------------------

# The theme to use for HTML and HTML Help pages.  See the documentation for
# a list of builtin themes.
#

# Add any paths that contain custom static files (such as style sheets) here,
# relative to this directory. They are copied after the builtin static files,
# so a file named "default.css" will overwrite the builtin "default.css".
html_static_path = ['_static']
html_logo = '_static/logo-ichthyop.svg'

# use figure numbers for referencing figures
numfig = True

# use sections as a reference for figures: X.1, X.2 with X the section
numfig_secnum_depth = (1)

import sphinx_rtd_theme
html_theme = "sphinx_rtd_theme"
html_theme_path = [sphinx_rtd_theme.get_html_theme_path()]

#html_context = {
#        'css_files': [
#            '_static/theme_overrides.css',  # override wide tables in RTD theme
#           ],
#        }
