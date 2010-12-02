::: Missing module implementation
includes EEPROM;

module super
{
  provides interface StdControl;

  uses {
    interface StdControl as SubControl;
  }
}
