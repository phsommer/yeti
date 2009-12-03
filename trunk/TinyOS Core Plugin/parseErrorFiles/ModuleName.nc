::: Missing module name
includes EEPROM;

module
{
  provides interface StdControl;

  uses {
    interface StdControl as SubControl;
  }
}
implementation
{
  event int EEPROMWrite.writeDone(int success) {
    eepromLineInUse = FALSE;
    checkForSaveId();
    return SUCCESS;
  }

}